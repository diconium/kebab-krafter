package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.generator.AUTO_GENERATOR_WARNING
import com.diconium.mobile.tools.kebabkrafter.generator.indent
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.ktor.server.routing.*
import java.io.File
import kotlin.reflect.KClass

object ServiceLocatorGenerator {

    fun generateServiceLocator(outputDirectory: File) {
        FileSpec.builder(PACKAGE, "ServiceLocator")
            .indent()
            .addFileComment(AUTO_GENERATOR_WARNING)
            .addType(serviceLocatorInterface())
            .build()
            .writeTo(outputDirectory)
    }

    private fun serviceLocatorInterface(): TypeSpec {
        val t = TypeVariableName("T", Any::class)
        return TypeSpec.interfaceBuilder("ServiceLocator")
            .addFunction(
                FunSpec.builder("getService")
                    .receiver(RoutingContext::class)
                    .addModifiers(KModifier.ABSTRACT)
                    .addTypeVariable(t)
                    .addParameter("type", KClass::class.asClassName().parameterizedBy(t))
                    .returns(t)
                    .build(),
            )
            .build()
    }

    private const val PACKAGE = "com.budius.kebabkrafter"

    val serviceLocatorClass = ClassName(PACKAGE, "ServiceLocator")
}
