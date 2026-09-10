package com.diconium.mobile.tools.kebabkrafter.generator.ktorclient

import com.diconium.mobile.tools.kebabkrafter.KebabLogger
import com.diconium.mobile.tools.kebabkrafter.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.*
import com.diconium.mobile.tools.kebabkrafter.models.ResponseType
import com.diconium.mobile.tools.kebabkrafter.requiresSupportClass
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.ktor.client.*

internal class KtorClientGenerator(
    private val log: KebabLogger,
    private val safeExecution: Boolean,
    private val basePackage: String,
) {

    init {
        log.d("Initing KtorClientGenerator for $basePackage")
    }

    fun generate(controller: KtorController): FileSpec {
        val poet = PoetController(basePackage, controller)

        val interfaceSpec = with(poet) {
            TypeSpec
                .funInterfaceBuilder(poet.controllerClassName)
                .markDeprecated(controller.deprecated)
                .addFunction(
                    FunSpec
                        .builder("invoke")
                        .addModifiers(KModifier.OPERATOR)
                        .markDeprecated(controller.deprecated)
                        .makeAbstractFunction()
                        .applySafeReturn(responseType())
                        .build(),
                )
                .build()
        }

        val supportClass = with(poet) {
            TypeSpec.classBuilder(poet.supportClassName)
                .makeSupportClass(true)
                .build()
                .takeIf { controller.response.requiresSupportClass }
        }

        return FileSpec.builder(poet.controllerClassName)
            .indent()
            .addFileComment(AUTO_GENERATOR_WARNING)
            .addImport("io.ktor.client.request", controller.ktorFunction)
            .addType(interfaceSpec)
            .addProperty(buildExtensionProperty(poet, controller))
            .apply { supportClass?.let { addType(it) } }
            .build()
    }

    private fun buildExtensionProperty(poet: PoetController, controller: KtorController) = PropertySpec
        .builder(controller.className.toCamelCase(), poet.controllerClassName)
        .apply { controller.kdoc?.let(::addKdoc) }
        .markDeprecated(controller.deprecated)
        .receiver(HttpClient::class)
        .getter(buildExtensionFunction(poet, controller).toBuilder(name = "get()").build())
        .build()

    private fun FunSpec.Builder.applySafeReturn(returnType: ClassName?): FunSpec.Builder = apply {
        if (safeExecution.not()) {
            return@apply
        }

        val type = if (returnType == null) {
            Result::class.parameterizedBy(Unit::class)
        } else {
            Result::class.asTypeName().parameterizedBy(returnType)
        }

        returns(type)
    }

    private fun CodeBlock.Builder.applyRunCatching(
        safeExecution: Boolean,
        block: CodeBlock.Builder.() -> Unit,
    ): CodeBlock.Builder = apply {
        if (safeExecution.not()) {
            block()
        } else {
            controlFlow("runCatching", block)
        }
    }

    private fun CodeBlock.Builder.controlFlow(
        controlFlow: String,
        block: CodeBlock.Builder.() -> Unit,
    ): CodeBlock.Builder = apply {
        beginControlFlow(controlFlow)
        block()
        endControlFlow()
    }

    private fun buildExtensionFunction(poet: PoetController, controller: KtorController): FunSpec {
        val parameters = poet.parameters()
        val types = buildList {
            add(poet.controllerClassName)
            addAll(parameters.map { it.type })
        }.toTypedArray()

        val lambdaArguments = parameters
            .joinToString(separator = ", ", prefix = " ", postfix = " ->") { p ->
                "${p.name}: %T"
            }
            .takeIf { parameters.isNotEmpty() } ?: ""

        return FunSpec
            .builder(controller.className.toCamelCase())
            .receiver(HttpClient::class)
            .addCode(
                CodeBlock
                    .builder()
                    .beginControlFlow("return %T {$lambdaArguments", *types)
                    .applyRunCatching(safeExecution) {
                        controlFlow("val response = ${controller.ktorFunction}") {
                            // headers
                            controller.routeHeaders.forEach { (key, value) ->
                                addStatement("%M(\"${key}\", \"${value}\")", fHeader)
                            }
                            // URL path + path parameters
                            controlFlow("url") {
                                controller.path.forEach { path ->
                                    if (path.startsWith("{") && path.endsWith("}")) {
                                        addStatement("%M(${path.trim('{', '}')})", fAppendEncodedPathSegments)
                                    } else {
                                        addStatement("%M(\"$path\")", fAppendPathSegments)
                                    }
                                }
                            }
                            // query parameters
                            parameters
                                .filter { it.usage == ParametersDef.Usage.Query }
                                .forEach { p ->
                                    addStatement("%M(\"${p.name}\", ${p.name})", fParameter)
                                }
                            // body
                            if (poet.requestClassName != null) {
                                addStatement("%M(%M)", fContentType, pJson)
                                addStatement("%M(body)", fSetBody)
                            }
                        }
                        if (controller.response.requiresSupportClass) {
                            addStatement("%T(", poet.supportClassName)
                            indent()
                            if (poet.responseClassName != null || controller.response.type == ResponseType.Binary) {
                                addStatement("body = response.%M(),", fBody)
                            }
                            controller.response.headers.forEach { (key, value) ->
                                addStatement("$value = response.headers[\"$key\"],")
                            }
                            unindent()
                            addStatement(")")
                        } else {
                            addStatement("response.%M()", fBody)
                        }
                    }
                    .endControlFlow()
                    .build(),
            )
            .build()
    }
}

private val fAppendPathSegments = MemberName("io.ktor.http", "appendPathSegments")
private val fAppendEncodedPathSegments = MemberName("io.ktor.http", "appendEncodedPathSegments")
private val fParameter = MemberName("io.ktor.client.request", "parameter")
private val fSetBody = MemberName("io.ktor.client.request", "setBody")
private val fHeader = MemberName("io.ktor.client.request", "header")
private val fBody = MemberName("io.ktor.client.call", "body")

private val fContentType = MemberName("io.ktor.http", "contentType")
private val pJson = MemberName(
    enclosingClassName = ClassName.bestGuess("io.ktor.http.ContentType.Application"),
    simpleName = "Json",
)
