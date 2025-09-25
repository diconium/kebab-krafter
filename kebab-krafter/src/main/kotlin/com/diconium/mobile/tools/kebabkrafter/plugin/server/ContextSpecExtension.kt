package com.diconium.mobile.tools.kebabkrafter.plugin.server

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

interface ContextSpecExtension {
    @get:Input
    val packageName: Property<String>

    @get:Input
    val className: Property<String>

    @get:Input
    val factoryName: Property<String>
}
