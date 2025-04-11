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

    internal var ktorTransformer: KtorTransformer = KtorTransformer { endpoint, controller -> controller }
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
     * The mapper is the most complex (and powerful) part of the transformer API, hence use is discouraged.
     * There is a [DefaultKtorControllerMapper] available that is used internally, but accessible for other mappers.
     */
    fun ktorMapper(t: KtorMapper) {
        ktorMapper = t
    }

    /**
     * Transforms individual [KtorController] after they have been mapped.
     * This is the last step before the actual code generator.
     *
     * The [Endpoint] provided here in this callback is for reference only.
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
    fun transform(endpoint: Endpoint, controller: KtorController): KtorController
}
