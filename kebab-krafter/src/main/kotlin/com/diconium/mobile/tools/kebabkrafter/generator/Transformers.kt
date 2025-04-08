package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi
import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint

@KebabKrafterUnstableApi
class Transformers {
    internal var endpointTransformer: EndpointTransformer = EndpointTransformer { endpoint -> endpoint }
        private set

    internal var ktorMapper: KtorMapper = DefaultKtorControllerMapper
        private set

    internal var ktorTransformer: KtorTransformer = KtorTransformer { endpoint -> endpoint }
        private set

    /**
     * Endpoints are the server routes extracted from the original swagger YML.
     * Endpoints contains HTTP related data such as path/method/headers/body.
     * This transformer allows to modify the endpoints before they are processed by the code generators.
     */
    fun endpointTransformer(t: EndpointTransformer) {
        endpointTransformer = t
    }

    /**
     * Mapper that converts the Endpoint to KtorController. The controller is the basis for the code generator,
     * Controller contains code related data such as package/class/kdoc.
     *
     * The mapper is the most complex (and powerful) part of the transformer API,
     * so the [DefaultKtorControllerMapper] is available for mappers to use.
     */
    fun ktorMapper(t: KtorMapper) {
        ktorMapper = t
    }

    /**
     * Transforms individual controllers after they have been mapped.
     * This is the last step before the actual code generator.
     */
    fun ktorTransformer(t: KtorTransformer) {
        ktorTransformer = t
    }
}

fun interface EndpointTransformer {
    fun transform(endpoint: Endpoint): Endpoint
}

fun interface KtorMapper {
    fun map(shortestPath: Int, dataSpecs: Map<String, BaseSpecModel>, endpoint: Endpoint): KtorController
}

fun interface KtorTransformer {
    fun transform(controller: KtorController): KtorController
}
