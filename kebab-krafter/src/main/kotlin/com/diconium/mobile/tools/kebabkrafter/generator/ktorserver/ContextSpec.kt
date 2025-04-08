package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.squareup.kotlinpoet.ClassName

class ContextSpec(
    val packageName: String,
    val className: String,
    val factoryName: String,
) {
    internal fun asClassName() = ClassName(packageName, className)
}
