package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import io.ktor.http.*

object DefaultKtorControllerMapper : KtorMapper {

	override fun map(shortestPath: Int, dataSpecs: Map<String, BaseSpecModel>, endpoint: Endpoint): KtorController {
		val method = endpoint.method.value.toPascalCase()
		val (packageName, className) = if (shortestPath > 2) {
			endpoint.path.splitPath(method, 2)
		} else if (shortestPath > 1) {
			endpoint.path.splitPath(method, 1)
		} else {
			val pathPart = endpoint.path
				.map { it.replace("{", "").replace("}", "") }
				.joinToString("") { it.toPascalCase() }
			"controllers" to "${method}$pathPart"
		}

		val kdoc = buildString {
			endpoint.description?.let { append("$it\n") }
			append(endpoint.method.value)
			append("/")
			append(endpoint.path.joinToString("/"))
		}

		val request = KtorController.Request(
			pathParameters = endpoint.pathParameters.toList(),
			queryParameters = endpoint.queryParameters.toList(),
			body = endpoint.bodyId?.let { dataSpecs[it] },
		)

		val response = KtorController.Response(
			body = endpoint.response.id?.let { dataSpecs[it] },
			status = endpoint.response.status,
			type = endpoint.response.type,
			contentTypeHeader = endpoint.response.contentTypeHeader,
			headers = endpoint.response.headers.map { it to "header${it.toPascalCase()}" },
		)

		return KtorController(
			ktorFunction = endpoint.method.function(),
			route = endpoint.path.joinToString("/"),
			routeHeaders = emptyList(),
			packageName = packageName,
			className = className,
			kdoc = kdoc,
			request = request,
			response = response,
		)
	}
}

private fun List<String>.splitPath(method: String, count: Int): Pair<String, String> {
	val packageName = "controllers.${take(count).joinToString(".").withoutBrackets}"
	val classNamePath = drop(count).joinToString("") { it.toPascalCase() }.withoutBrackets
	return packageName to "${method}$classNamePath"
}

private val String.withoutBrackets: String
	get() = this.replace("{", "").replace("}", "")

private fun HttpMethod.function() = when (this) {
	HttpMethod.Get -> "get"
	HttpMethod.Post -> "post"
	HttpMethod.Put -> "put"
	HttpMethod.Patch -> "patch"
	HttpMethod.Delete -> "delete"
	HttpMethod.Head -> "put"
	HttpMethod.Options -> "options"
	else -> throw IllegalArgumentException("No method $this")
}
