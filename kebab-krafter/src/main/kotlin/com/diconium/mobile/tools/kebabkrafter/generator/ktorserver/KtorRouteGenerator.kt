package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.generator.*
import com.diconium.mobile.tools.kebabkrafter.generator.AUTO_GENERATOR_WARNING
import com.diconium.mobile.tools.kebabkrafter.generator.PoetController
import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.ResponseType
import com.diconium.mobile.tools.kebabkrafter.models.UrlType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.ktor.http.*
import io.ktor.server.routing.*
import java.io.File
import kotlin.reflect.KClass

class KtorRouteGenerator(
    private val basePackage: String,
    private val context: ContextSpec,
    private val outputDirectory: File,
) {

    fun generate(installFunction: String, controllers: List<KtorController>) {
        FileSpec.builder(basePackage, "InstallRoutes")
            .indent()
            .addFileComment(AUTO_GENERATOR_WARNING)
            .addImport("io.ktor.server.routing", ktorRouteFunctions)
            .addImport("io.ktor.server.application", "call")
            .addImport("io.ktor.server.request", "receive")
            .addImport("io.ktor.server.response", "respond", "respondOutputStream", "header")
            .addImport("io.ktor.http", "HttpStatusCode", "ContentType")
            .addImport(context.packageName, context.className)
            .addFunction(installFunction(installFunction, controllers))
            .addFunction(serviceLocatorGet(serviceLocatorClass))
            .addType(serviceLocatorInterface())
            .build()
            .writeTo(outputDirectory)
    }

    private fun installFunction(name: String, controllers: List<KtorController>) = FunSpec
        .builder(name)
        .receiver(Route::class)
        .addParameter("locator", serviceLocatorClass)
        .addCode(installFunctionCode(controllers))
        .build()

    private fun installFunctionCode(controllers: List<KtorController>) = CodeBlock.builder().apply {
        controllers.forEach { controller ->
            generate(controller)
        }
    }.build()

    private fun CodeBlock.Builder.generate(controller: KtorController) {
        fun CodeBlock.Builder.generateHeader(routeHeaders: MutableList<Pair<String, String>>) {
            if (routeHeaders.isEmpty()) {
                controlFlow("${controller.ktorFunction}(\"${controller.route}\")") {
                    generateCall(controller)
                }
            } else {
                val (headerKey, value) = routeHeaders.removeFirst()
                controlFlow("header(\"$headerKey\", \"$value\")") {
                    generateHeader(routeHeaders)
                }
            }
        }

        generateHeader(controller.routeHeaders.toMutableList())
    }

    private fun CodeBlock.Builder.generateCall(controller: KtorController) {
        val poet = PoetController(basePackage, controller)
        addStatement("val context = ${context.className}.${context.factoryName}(call)")
        addStatement("val controller = locator.get<%T>()", poet.controllerClassName)

        val inputs = mutableListOf<String>()
        controller.request.pathParameters.forEach { (name, type) ->
            addStatement("val $name = ${type.toValue("call.parameters[\"$name\"]")}")
            if (type.required) {
                withIndent {
                    addStatement("?: throw NullPointerException(\"$name is a required query parameter\")")
                }
            }
            inputs.add(name)
        }
        controller.request.queryParameters.forEach { (name, type) ->
            addStatement("val $name = ${type.toValue("call.request.queryParameters[\"$name\"]")}")
            if (type.required) {
                withIndent {
                    addStatement("?: throw NullPointerException(\"$name is a required query parameter\")")
                }
            }
            inputs.add(name)
        }
        controller.request.body?.let { body: BaseSpecModel ->
            val type = ClassName("$basePackage.${body.relativePackageName}", body.name)
            addStatement("val body = call.receive<%T>()", type)
            inputs.add("body")
        }
        val resultField = if (
            controller.response.body == null &&
            controller.response.requiresSupportClass.not() &&
            controller.response.type == ResponseType.Json
        ) {
            ""
        } else {
            "val result = "
        }
        addStatement("${resultField}with(controller) { context.execute(${inputs.joinToString(", ")}) }")
        controller.response.headers.forEach { (key, value) ->
            addStatement("call.response.header(name = \"${key}\", value = result.$value)")
        }
        when (controller.response.type) {
            ResponseType.Json -> generateJsonResponse(controller.response)
            ResponseType.Binary -> generateBinaryResponse(controller.response)
        }
    }

    private val serviceLocatorClass = ClassName(basePackage, "ServiceLocator")
}

private fun CodeBlock.Builder.controlFlow(controlFlow: String, vararg args: Any?, block: CodeBlock.Builder.() -> Unit) {
    beginControlFlow(controlFlow, args)
    block()
    endControlFlow()
}

private fun UrlType.toValue(input: String): String = when (format) {
    UrlType.Format.String -> input
    UrlType.Format.Int -> "$input?.toIntOrNull()"
    UrlType.Format.Boolean -> "$input?.toBoolean()"
    UrlType.Format.Float -> "$input?.toFloat()"
    UrlType.Format.StringArray -> "$input?.split(\",\")"
}

private fun CodeBlock.Builder.generateJsonResponse(response: KtorController.Response) {
    val result = when {
        response.body == null -> ""
        response.headers.isNotEmpty() -> ", result.body"
        else -> ", result"
    }

    addStatement("call.respond(HttpStatusCode.${response.status.codeDescription}$result)")
}

private val HttpStatusCode.codeDescription: String
    get() = description
        .replace(" ", "")
        .replace("-", "")

private fun CodeBlock.Builder.generateBinaryResponse(response: KtorController.Response) {
    val contentType = response.contentTypeHeader?.let { raw ->
        wellKnownContentType[raw]?.let { wellKnown ->
            "(contentType = $wellKnown)"
        } ?: "(contentType = ContentType.parse(\"${response.contentTypeHeader}\"))"
    } ?: ""

    controlFlow("call.respondOutputStream$contentType") {
        val bodyFlow = if (response.requiresSupportClass) {
            "result.body.use { incoming ->"
        } else {
            "result.use { incoming ->"
        }
        controlFlow(bodyFlow) {
            addStatement("incoming.copyTo(this)")
        }
    }
}

private fun serviceLocatorGet(serviceLocatorClass: ClassName): FunSpec {
    val t = TypeVariableName("T", Any::class).copy(reified = true)
    return FunSpec.builder("get")
        .receiver(serviceLocatorClass)
        .addModifiers(KModifier.PRIVATE, KModifier.INLINE)
        .addTypeVariable(t)
        .addCode("return this.getService(T::class)")
        .returns(t)
        .build()
}

private fun serviceLocatorInterface(): TypeSpec {
    val t = TypeVariableName("T", Any::class)
    return TypeSpec.interfaceBuilder("ServiceLocator")
        .addFunction(
            FunSpec.builder("getService")
                .addModifiers(KModifier.ABSTRACT)
                .addTypeVariable(t)
                .addParameter("type", KClass::class.asClassName().parameterizedBy(t))
                .returns(t)
                .build(),
        )
        .build()
}

private val ktorRouteFunctions = listOf("header", "get", "post", "put", "patch", "delete", "put", "options")

private val wellKnownContentType = mapOf(
    "application/*" to "ContentType.Application.Any",
    "application/json" to "ContentType.Application.Json",
    "application/pdf" to "ContentType.Application.Pdf",
    "application/xml" to "ContentType.Application.Xml",
    "application/zip" to "ContentType.Application.Zip",
    "application/gzip" to "ContentType.Application.GZip",

    "image/*" to "ContentType.Image.Any",
    "image/png" to "ContentType.Image.PNG",
    "image/jpeg" to "ContentType.Image.JPEG",
    "image/gif" to "ContentType.Image.GIF",
)
