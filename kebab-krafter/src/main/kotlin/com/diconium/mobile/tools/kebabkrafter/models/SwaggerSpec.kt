package com.diconium.mobile.tools.kebabkrafter.models

import com.diconium.mobile.tools.kebabkrafter.Log
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

data class SwaggerSpec(val baseUrl: String, val endpoints: List<Endpoint>, val dataSpecs: Map<String, BaseSpecModel>)

data class Endpoint(
    val path: List<String>,
    val description: String?,
    val method: HttpMethod,
    val tags: List<String>,
    val response: Response,
    val queryParameters: Map<String, UrlType> = emptyMap(),
    val pathParameters: Map<String, UrlType> = emptyMap(),
    val routeHeaders: List<Pair<String, String>> = emptyList(),
    val version: String? = null,
    val bodyId: String? = null,
)

data class Response(
    val id: String?,
    val status: HttpStatusCode,
    val type: ResponseType,
    val contentTypeHeader: String?,
    val headers: List<String>,
)

data class UrlType(val required: Boolean, val format: Format) {
    enum class Format {
        String,
        Int,
        Boolean,
        Float,
        StringArray,
    }
}

fun SwaggerSpec.log() {
    Log.d("SwaggerSpec:")
    Log.d("  baseUrl: $baseUrl")
    Log.d("  endpoints:")
    endpoints.forEach { Log.d("    - $it") }
    Log.d("  models:")
    dataSpecs.forEach { Log.d("    - $it") }
}
