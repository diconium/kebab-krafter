package com.diconium.mobile.tools.kebabkrafter.plugin.client

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

interface KtorClientExtension {

    /**
     * True to enable logging; false otherwise
     * defaults to: false
     */
    @get:Console
    val log: Property<Boolean>

    @get:Nested
    @get:Input
    val services: NamedDomainObjectContainer<KtorClientServiceExtension>

    /**
     * Invoke this function to create and configure a KtorClient
     */
    fun create(name: String, block: Action<KtorClientServiceExtension>) {
        services.create(name, block)
    }
}
