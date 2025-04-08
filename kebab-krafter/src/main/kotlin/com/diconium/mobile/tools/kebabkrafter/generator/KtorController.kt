package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.ResponseType
import com.diconium.mobile.tools.kebabkrafter.models.UrlType
import io.ktor.http.*

data class KtorController(
    val ktorFunction: String,
    val route: String,
    val routeHeaders: List<Pair<String, String>>,
    val packageName: String,
    val className: String,
    val kdoc: String?,
    val request: Request,
    val response: Response,
) {
    data class Request(
        val pathParameters: List<Pair<String, UrlType>>,
        val queryParameters: List<Pair<String, UrlType>>,
        val body: BaseSpecModel?,
    )

    data class Response(
        val body: BaseSpecModel?,
        val status: HttpStatusCode,
        val type: ResponseType,
        val contentTypeHeader: String?,
        val headers: List<Pair<String, String>>,
    )
}

val KtorController.Response.requiresSupportClass: Boolean
    get() = this.headers.isNotEmpty()
