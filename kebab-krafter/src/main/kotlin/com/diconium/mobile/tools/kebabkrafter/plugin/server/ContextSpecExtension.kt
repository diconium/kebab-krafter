package com.diconium.mobile.tools.kebabkrafter.plugin.server

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import javax.inject.Inject

/**
 * Specification for the custom context where and API call is executed
 */
open class ContextSpecExtension @Inject constructor(objects: ObjectFactory) {
    /**
     * Package name of the custom context
     */
    @get:Input
    val packageName: Property<String> = objects.property(String::class.java)

    /**
     * Class name of the custom context
     */
    @get:Input
    val className: Property<String> = objects.property(String::class.java)

    /**
     * Name of the factory method to build a new context
     */
    @get:Input
    val factoryName: Property<String> = objects.property(String::class.java)
}
