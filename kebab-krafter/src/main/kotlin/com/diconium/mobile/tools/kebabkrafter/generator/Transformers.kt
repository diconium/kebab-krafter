package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint

internal class Transformers(
    val endpointTransformer: EndpointTransformer,
    val ktorMapper: KtorMapper,
    val ktorTransformer: KtorTransformer,
)

fun interface EndpointTransformer {
    fun transform(endpoint: Endpoint): Endpoint
}

fun interface KtorMapper {
    fun map(shortestPath: Int, dataSpecs: Map<String, BaseSpecModel>, endpoint: Endpoint): KtorController
}

fun interface KtorTransformer {
    fun transform(endpoint: Endpoint, controller: KtorController): KtorController
}
