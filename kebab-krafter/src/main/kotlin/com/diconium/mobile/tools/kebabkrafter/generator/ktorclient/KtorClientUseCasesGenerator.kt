package com.diconium.mobile.tools.kebabkrafter.generator.ktorclient

import com.diconium.mobile.tools.kebabkrafter.generator.*
import com.diconium.mobile.tools.kebabkrafter.generator.AUTO_GENERATOR_WARNING
import com.diconium.mobile.tools.kebabkrafter.generator.PoetController
import com.diconium.mobile.tools.kebabkrafter.generator.indent
import com.diconium.mobile.tools.kebabkrafter.models.ResponseType
import com.diconium.mobile.tools.kebabkrafter.models.UrlType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.ktor.client.*
import io.ktor.client.request.*
import java.io.InputStream

class KtorClientUseCasesGenerator(
	private val basePackage: String,
) {

	fun generate(controller: KtorController): FileSpec {

		val poet = PoetController(basePackage, controller)

		val response = when (controller.response.type) {
			ResponseType.Json -> poet.responseClassName
			ResponseType.Binary -> InputStream::class.asTypeName()
		} ?: Unit::class.asTypeName()

		val returnType = Result::class.asTypeName().parameterizedBy(response)

		// fun build(client: HttpClient): <UseCaseType>
		val build = FunSpec
			.builder("build")
			.addParameter("client", HttpClient::class)
			.addCode(CodeBlock.Builder().apply {
				if (controller.response.type == ResponseType.Binary) {
					addStatement("TODO(\"Handle binary\")")
				} else {
					controlFlow("return ${controller.className} { ${parameterNames(controller)} ->") {
						handleJson(controller)
					}
				}
			}.build())
			.returns(poet.controllerClassName)
			.build()

		// companion object Factory {
		val factory = TypeSpec.companionObjectBuilder("Factory")
			.addFunction(build)
			.build()

		// suspend operator fun invoke(<params...>) : <ResponseType>
		val invoke = FunSpec.builder("invoke")
			.addModifiers(KModifier.SUSPEND, KModifier.OPERATOR, KModifier.ABSTRACT)
			.addParameters(controller)
			.returns(returnType)
			.build()

		//fun interface <Type>
		val funInterface = TypeSpec.funInterfaceBuilder(controller.className)
			.addFunction(invoke)
			.apply { controller.kdoc?.let(::addKdoc) }
			.addType(factory)
			.build()

		return FileSpec.builder(poet.controllerClassName)
			.indent()
			.addFileComment(AUTO_GENERATOR_WARNING)
			.addImport("io.ktor.client.request", controller.ktorFunction)
			.apply {
				if (controller.request.body != null) {
					addImport("io.ktor.client.request", "setBody")
				}
			}
			.addImport("io.ktor.client.call", "body")
			.addImport("io.ktor.http", "appendPathSegments")
			.addType(funInterface)
			.build()
	}

	private fun FunSpec.Builder.addParameters(controller: KtorController) = apply {
		controller.request.pathParameters.forEach { (name, type) ->
			addParameter(name, type.toTypeName())
		}
		controller.request.queryParameters.forEach { (name, type) ->
			addParameter(name, type.toTypeName())
		}
		val poet = PoetController(basePackage, controller)

		poet.requestClassName?.let {
			addParameter("body", it)
		}
	}

	private fun parameterNames(controller: KtorController) = buildString {
		controller.request.pathParameters.forEach { (name, _) ->
			append(name)
		}
		controller.request.queryParameters.forEach { (name, _) ->
			if (this.isNotEmpty()) append(", ")
			append(name)
		}
		controller.request.body?.let {
			if (this.isNotEmpty()) append(", ")
			append("body")
		}
	}

	private fun CodeBlock.Builder.handleJson(controller: KtorController) {
		controlFlow("runCatching") {
			controlFlow("val request = client.${controller.ktorFunction}") {
				// URL
				addUrl(controller)

				// headers
				controller.routeHeaders.forEach { (key, value) ->
					addStatement("header(\"$key\", \"$value\")")
				}

				// body
				if (controller.request.body != null) {
					addStatement("setBody(body)")
				}
			}

			// execute call
			addStatement("request.body()")
		}
	}

	private fun CodeBlock.Builder.addUrl(controller: KtorController) {

		fun UrlType.withToString() = if (format != UrlType.Format.String) ".toString()" else ""

		controlFlow("url") {

			// path
			val pathParams: Map<String, UrlType> = controller.request.pathParameters.associate { it }
			val queryParams: List<Pair<String, UrlType>> = controller.request.queryParameters

			// route
			controller.route
				.split("/")
				.forEach { segment ->
					if (segment.startsWith('{') && segment.endsWith('}')) {
						val name = segment.trim('{', '}')
						val type = pathParams[name]
						if (type == null) {
							throw IllegalStateException("Declared segment $segment is not listed in the pathParameters")
						} else {
							addStatement("appendPathSegments(${name}${type.withToString()})")
						}
					} else {
						addStatement("appendPathSegments(\"$segment\")")
					}
				}

			// query
			queryParams.forEach { (name, type) ->
				if (type.required) {
					addStatement("parameters.append(\"${name}\", ${name})")
				} else {
					addStatement("${name}?.let { parameters.append(\"${name}\", it${type.withToString()}) }")
				}
			}
		}
	}
}
