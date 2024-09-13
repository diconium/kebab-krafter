package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.models.UrlType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

/**
 * Externalize simple class, package name concatenation and formation.
 */
internal class PoetController(
	private val basePackage: String,
	controller: KtorController,
) {

	val controllerClassName = ClassName("$basePackage.${controller.packageName}", controller.className)
	val requestClassName = controller.request.body?.let { requestBody ->
		ClassName("$basePackage.${requestBody.relativePackageName}", requestBody.name)
	}
	val responseClassName = controller.response.body?.let { responseBody ->
		ClassName("$basePackage.${responseBody.relativePackageName}", responseBody.name)
	}
	val supportClassName = ClassName("$basePackage.${controller.packageName}", "${controller.className}Response")
}

internal fun UrlType.toTypeName(): TypeName {
	return when (format) {
		UrlType.Format.String -> String::class.asTypeName()
		UrlType.Format.Int -> Int::class.asTypeName()
		UrlType.Format.Boolean -> Boolean::class.asTypeName()
		UrlType.Format.Float -> Float::class.asTypeName()
		UrlType.Format.StringArray -> List::class.asTypeName().parameterizedBy(String::class.asTypeName())
	}.copy(nullable = required.not())
}

internal fun CodeBlock.Builder.controlFlow(controlFlow: String, vararg args: Any?, block: CodeBlock.Builder.() -> Unit) {
	beginControlFlow(controlFlow, args)
	block()
	endControlFlow()

}
