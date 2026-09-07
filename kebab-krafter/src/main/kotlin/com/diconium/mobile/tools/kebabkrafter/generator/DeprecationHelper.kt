package com.diconium.mobile.tools.kebabkrafter.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

fun TypeSpec.Builder.markDeprecated(deprecated: Boolean): TypeSpec.Builder = if (deprecated) {
    this.addAnnotation(deprecatedEndpoint)
} else {
    this
}

fun FunSpec.Builder.markDeprecated(deprecated: Boolean): FunSpec.Builder = if (deprecated) {
    this.addAnnotation(deprecatedEndpoint)
} else {
    this
}

fun PropertySpec.Builder.markDeprecated(deprecated: Boolean): PropertySpec.Builder = if (deprecated) {
    this.addAnnotation(deprecatedEndpoint)
} else {
    this
}

fun PropertySpec.Builder.markFieldDeprecated(deprecated: Boolean): PropertySpec.Builder = if (deprecated) {
    this.addAnnotation(deprecatedField)
} else {
    this
}

private val deprecatedEndpoint = AnnotationSpec.builder(Deprecated::class)
    .addMember("message = %S", "This endpoint is deprecated")
    .build()

private val deprecatedField = AnnotationSpec.builder(Deprecated::class)
    .addMember("message = %S", "This field is deprecated")
    .build()
