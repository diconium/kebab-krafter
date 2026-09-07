package com.diconium.mobile.tools.kebabkrafter

import com.diconium.mobile.tools.kebabkrafter.models.BaseJsonType
import com.diconium.mobile.tools.kebabkrafter.models.ResponseType
import com.diconium.mobile.tools.kebabkrafter.models.UrlType
import io.ktor.http.*

data class KtorController(
    val ktorFunction: String,
    val path: List<String>,
    val routeHeaders: List<Pair<String, String>>,
    val authentication: List<String>,
    val packageName: String,
    val className: String,
    val kdoc: String?,
    val request: Request,
    val response: Response,
    val deprecated: Boolean,
) {
    data class Request(
        val pathParameters: List<Pair<String, UrlType>>,
        val queryParameters: List<Pair<String, UrlType>>,
        val body: BaseJsonType?,
    )

    data class Response(
        val body: BaseJsonType?,
        val status: HttpStatusCode,
        val type: ResponseType,
        val contentTypeHeader: String?,
        val headers: List<Pair<String, String>>,
    )
}

val KtorController.Response.requiresSupportClass: Boolean
    get() = this.headers.isNotEmpty()
