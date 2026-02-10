package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.squareup.kotlinpoet.ClassName

/**
 * Externalize simple class, package name concatenation and formation.
 */
internal class PoetController(private val basePackage: String, controller: KtorController) {

    val controllerClassName = ClassName("$basePackage.${controller.packageName}", controller.className)
    val requestClassName = controller.request.body?.let { requestBody ->
        ClassName("$basePackage.${requestBody.relativePackageName.normalize()}", requestBody.name)
    }
    val responseClassName by lazy {
        controller.response.body?.let { responseBody ->
            ClassName("$basePackage.${responseBody.relativePackageName.normalize()}", responseBody.name)
        }
    }
    val supportClassName =
        ClassName("$basePackage.${controller.packageName.normalize()}", "${controller.className}Response")
}

private fun String.normalize() = this // .replace("./", ".").replace("..", ".")
