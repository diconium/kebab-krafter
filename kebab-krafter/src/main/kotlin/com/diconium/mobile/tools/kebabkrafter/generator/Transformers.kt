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

    fun endpointTransformer(t: EndpointTransformer) {
        endpointTransformer = t
    }

    fun ktorMapper(t: KtorMapper) {
        ktorMapper = t
    }

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
