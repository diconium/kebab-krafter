package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.generator.*
import com.diconium.mobile.tools.kebabkrafter.models.ResponseType
import com.squareup.kotlinpoet.*
import java.io.InputStream

class KtorServerUseCasesGenerator(private val basePackage: String, private val context: ClassName) {

	fun generate(controller: KtorController): FileSpec {
		val poet = PoetController(basePackage, controller)

		val function = FunSpec
			.builder("execute")
			.receiver(context)
			.apply { makeFunction(controller) }
			.build()

		val supportClass = TypeSpec.classBuilder(poet.supportClassName)
			.apply { makeSupportClass(controller) }
			.build()
			.takeIf { controller.response.serverRequiresSupportClass }

		return FileSpec.builder(poet.controllerClassName)
			.indent()
			.addFileComment(AUTO_GENERATOR_WARNING)
			.addType(TypeSpec.interfaceBuilder(poet.controllerClassName).addFunction(function).build())
			.apply { supportClass?.let { addType(it) } }
			.build()
	}

	private fun FunSpec.Builder.makeFunction(controller: KtorController) {
		controller.kdoc?.let(::addKdoc)
		addModifiers(KModifier.ABSTRACT, KModifier.SUSPEND)

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

		val response = if (controller.response.serverRequiresSupportClass) {
			poet.supportClassName
		} else {
			when (controller.response.type) {
				ResponseType.Json -> poet.responseClassName
				ResponseType.Binary -> InputStream::class.asTypeName()
			}
		}

		response?.let { returns(it) }
	}

	private fun TypeSpec.Builder.makeSupportClass(controller: KtorController) {
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
							.builder(value, String::class.asTypeName())
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
				PropertySpec.builder(name = value, type = String::class.asTypeName())
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
}
