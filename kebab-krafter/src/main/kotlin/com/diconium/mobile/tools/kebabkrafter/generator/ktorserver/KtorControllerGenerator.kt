package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.*
import com.diconium.mobile.tools.kebabkrafter.requiresSupportClass
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

class KtorControllerGenerator(private val basePackage: String, private val context: ClassName) {

    fun generate(controller: KtorController): FileSpec {
        val poet = PoetController(basePackage, controller)

        val function = with(poet) {
            FunSpec
                .builder("execute")
                .receiver(context)
                .makeAbstractFunction()
                .build()
        }

        val supportClass = with(poet) {
            TypeSpec.classBuilder(poet.supportClassName)
                .makeSupportClass(false)
                .build()
                .takeIf { controller.response.requiresSupportClass }
        }

        return FileSpec.builder(poet.controllerClassName)
            .indent()
            .addFileComment(AUTO_GENERATOR_WARNING)
            .addType(TypeSpec.interfaceBuilder(poet.controllerClassName).addFunction(function).build())
            .apply { supportClass?.let { addType(it) } }
            .build()
    }
}
