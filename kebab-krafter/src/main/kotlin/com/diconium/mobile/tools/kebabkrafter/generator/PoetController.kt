package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.KtorController
import com.diconium.mobile.tools.kebabkrafter.models.ResponseType
import com.diconium.mobile.tools.kebabkrafter.models.UrlType
import com.diconium.mobile.tools.kebabkrafter.requiresSupportClass
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.InputStream

/**
 * Externalize simple class, package name concatenation and formation.
 */
internal class PoetController(private val basePackage: String, private val controller: KtorController) {

    val controllerClassName = ClassName("$basePackage.${controller.packageName}", controller.className.toPascalCase())
    val requestClassName = controller.request.body?.let { requestBody ->
        ClassName("$basePackage.${requestBody.relativePackageName.normalize()}", requestBody.name.toPascalCase())
    }
    val responseClassName by lazy {
        controller.response.body?.let { responseBody ->
            ClassName("$basePackage.${responseBody.relativePackageName.normalize()}", responseBody.name.toPascalCase())
        }
    }
    val supportClassName =
        ClassName("$basePackage.${controller.packageName.normalize()}", "${controller.className}Response")

    internal fun FunSpec.Builder.makeAbstractFunction(): FunSpec.Builder = apply {
        controller.kdoc?.let(::addKdoc)
        addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)

        parameters().forEach { p ->
            addParameter(p.name, p.type)
        }

        responseType()?.let { returns(it) }
    }

    internal fun responseType(): ClassName? = if (controller.response.requiresSupportClass) {
        supportClassName
    } else {
        when (controller.response.type) {
            ResponseType.Json -> responseClassName
            ResponseType.Binary -> InputStream::class.asTypeName()
        }
    }

    internal fun TypeSpec.Builder.makeSupportClass(nullableHeaders: Boolean): TypeSpec.Builder = apply {
        with(modifiers) {
            remove(KModifier.PUBLIC)
            add(KModifier.DATA)
        }

        val body = PoetController(basePackage, controller).responseClassName

        primaryConstructor(
            FunSpec.constructorBuilder().apply {
                // headers (if any)
                addParameters(
                    controller.response.headers.map { (_, value) ->
                        ParameterSpec
                            .builder(value, String::class.asTypeName().copy(nullable = nullableHeaders))
                            .build()
                    },
                )

                if (controller.response.type == ResponseType.Binary) {
                    // input stream (if any)
                    addParameter(
                        ParameterSpec
                            .builder("body", InputStream::class.asTypeName())
                            .build(),
                    )
                } else if (body != null) {
                    // json body (if any)
                    addParameter(
                        ParameterSpec
                            .builder("body", body)
                            .build(),
                    )
                }
            }.build(),
        )

        addProperties(
            controller.response.headers.map { (_, value) ->
                PropertySpec.builder(name = value, type = String::class.asTypeName().copy(nullable = nullableHeaders))
                    .initializer(format = value)
                    .build()
            },
        )

        if (controller.response.type == ResponseType.Binary) {
            // input stream (if any)
            addProperty(
                PropertySpec.builder(name = "body", type = InputStream::class.asTypeName())
                    .initializer("body")
                    .build(),
            )
        } else if (body != null) {
            // json body (if any)
            addProperty(
                PropertySpec.builder(name = "body", type = body)
                    .initializer(format = "body")
                    .build(),
            )
        }
    }

    internal fun parameters() = buildList {
        controller.request.pathParameters.forEach { (name, type) ->
            add(ParametersDef(ParametersDef.Usage.Path, name, type.toTypeName()))
        }
        controller.request.queryParameters.forEach { (name, type) ->
            add(ParametersDef(ParametersDef.Usage.Query, name, type.toTypeName()))
        }
        requestClassName?.let {
            add(ParametersDef(ParametersDef.Usage.Body, "body", it))
        }
    }
}

internal data class ParametersDef(val usage: Usage, val name: String, val type: TypeName) {
    enum class Usage {
        Query,
        Path,
        Body,
    }
}

private fun String.normalize() = this // .replace("./", ".").replace("..", ".")

internal fun UrlType.toTypeName(): TypeName = when (format) {
    UrlType.Format.String -> String::class.asTypeName()
    UrlType.Format.Int -> Int::class.asTypeName()
    UrlType.Format.Boolean -> Boolean::class.asTypeName()
    UrlType.Format.Float -> Float::class.asTypeName()
    UrlType.Format.StringArray -> List::class.asTypeName().parameterizedBy(String::class.asTypeName())
}.copy(nullable = required.not())
