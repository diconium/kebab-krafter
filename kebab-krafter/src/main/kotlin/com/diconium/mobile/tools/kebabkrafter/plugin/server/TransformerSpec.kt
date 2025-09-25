package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi
import com.diconium.mobile.tools.kebabkrafter.generator.EndpointTransformer
import com.diconium.mobile.tools.kebabkrafter.generator.KtorMapper
import com.diconium.mobile.tools.kebabkrafter.generator.KtorTransformer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@KebabKrafterUnstableApi
interface TransformerSpec {
    @get:Optional
    @get:Input
    val endpointTransformer: Property<Class<out EndpointTransformer>>

    /**
     * Endpoints are the server routes extracted from the original swagger YML.
     * Endpoints contains HTTP related data such as path/method/headers/body.
     * This transformer allows to modify the endpoints before they are processed by the code generators.
     */
    fun endpointTransformer(block: EndpointTransformer) {
        endpointTransformer.set(block::class.java)
    }

    /**
     * Mapper that converts the Endpoint to KtorController. The controller is the basis for the code generator,
     * Controller contains code related data such as package/class/kdoc.
     *
     * The mapper is the most complex (and powerful) part of the transformer API, hence use is discouraged.
     * There is a [com.diconium.mobile.tools.kebabkrafter.generator.DefaultKtorControllerMapper] available that is used internally, but accessible for other mappers.
     */
    @get:Optional
    @get:Input
    val ktorMapper: Property<Class<out KtorMapper>>


    fun ktorMapper(block: KtorMapper) {
        ktorMapper.set(block::class.java)
    }

    @get:Optional
    @get:Input
    val ktorTransformer: Property<Class<out KtorTransformer>>

    /**
     * Transforms individual [com.diconium.mobile.tools.kebabkrafter.generator.KtorController] after they have been mapped.
     * This is the last step before the actual code generator.
     *
     * The [com.diconium.mobile.tools.kebabkrafter.models.Endpoint] provided here in this callback is for reference only.
     */
    fun ktorTransformer(block: KtorTransformer) {
        ktorTransformer.set(block::class.java)
    }
}
