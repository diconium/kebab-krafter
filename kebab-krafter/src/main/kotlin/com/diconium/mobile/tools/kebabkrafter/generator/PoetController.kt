package com.diconium.mobile.tools.kebabkrafter.generator

import com.squareup.kotlinpoet.ClassName

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
