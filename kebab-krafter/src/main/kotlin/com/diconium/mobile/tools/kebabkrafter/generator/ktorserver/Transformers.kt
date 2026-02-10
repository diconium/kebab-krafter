package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import com.diconium.mobile.tools.kebabkrafter.models.JsonSpecFile

internal class Transformers(
    val endpointTransformer: EndpointTransformer,
    val ktorMapper: KtorMapper,
    val ktorTransformer: KtorTransformer,
)

fun interface EndpointTransformer {
    fun transform(endpoint: Endpoint): Endpoint
}

fun interface KtorMapper {
    fun map(shortestPath: Int, endpoint: Endpoint, dataSpecs: Map<String, JsonSpecFile>): KtorController
}

fun interface KtorTransformer {
    fun transform(endpoint: Endpoint, controller: KtorController): KtorController
}
