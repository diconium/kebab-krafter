package com.diconium.mobile.tools.kebabkrafter.parser

import com.diconium.mobile.tools.kebabkrafter.KebabLogger
import com.diconium.mobile.tools.kebabkrafter.models.*
import com.diconium.mobile.tools.kebabkrafter.parser.json.JsonParser
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.fromValue
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.core.models.SwaggerParseResult
import java.io.File

internal class SwaggerParser(private val log: KebabLogger) {

    private fun generateEndpoint(
        key: String,
        method: HttpMethod,
        operation: Operation,
        pathParameters: List<Parameter>?,
    ): Endpoint {
        log.d("Parsing endpoint $key")

        fun findUrlTypeFormat(type: String) = when (type) {
            "string" -> UrlType.Format.String
            "number" -> UrlType.Format.Float
            "integer" -> UrlType.Format.Int
            "boolean" -> UrlType.Format.Boolean
            "array" -> UrlType.Format.StringArray
            else -> throw IllegalArgumentException("Unsupported URL type format: $type")
        }

        fun paramMapper(param: Parameter) = param.name to UrlType(
            required = param.required == true,
            format = (param.schema?.type ?: "string").let(::findUrlTypeFormat),
        )

        val parameters = buildList {
            operation.parameters?.let(::addAll)
            pathParameters?.let(::addAll)
        }

        return Endpoint(
            path = key.split("/").filter { it.isNotBlank() },
            method = method,
            tags = operation.tags ?: emptyList(),
            queryParameters = parameters.filter { it.`in` == "query" }.associate(::paramMapper),
            pathParameters = parameters.filter { it.`in` == "path" }.associate(::paramMapper),
            response = parseSuccessResponse(operation),
            description = operation.summary,
            bodyId = operation.requestBody?.content?.get("application/json")?.schema?.`$ref`
                ?.takeIf { it.endsWith(".json") },
            errorResponseIds = parseFailureResponses(operation),
        )
    }

    private fun parseFailureResponses(operation: Operation): Map<HttpStatusCode, String> {
        return operation
            .responses
            .entries
            .mapNotNull { (key, value) ->
                val httpStatus = fromValue(key.toInt())
                val isFailure = httpStatus.value in (400 until 600)
                val responseId = value.content?.get("application/json")?.schema?.`$ref`?.takeIf { it.endsWith(".json") }
                if (responseId != null && isFailure) {
                    httpStatus to responseId
                } else {
                    null
                }
            }
            // associate
            .associate { it }
    }

    private fun parseSuccessResponse(operation: Operation): Response {
        val successCount = operation.responses.entries.count { fromValue(it.key.toInt()).isSuccess() }
        if (successCount != 1) throw IllegalArgumentException("We only support one success type")
        val entry = operation.responses.entries.firstOrNull { fromValue(it.key.toInt()).isSuccess() }
        val content = entry?.value?.content
        val headers = entry?.value?.headers?.map { it.key }.orEmpty()
        val responseStatus =
            entry?.key?.toInt() ?: throw NullPointerException("Missing response status for ${operation.responses}")

        val responseId = content?.get("application/json")?.schema?.`$ref`?.takeIf { it.endsWith(".json") }
        val isBinary = content?.toList()?.first()?.second?.schema?.format == "binary"

        return if (isBinary) {
            Response(
                id = null,
                status = fromValue(responseStatus),
                type = ResponseType.Binary,
                contentTypeHeader = content.toList().first().first,
                headers = headers,
            )
        } else {
            Response(
                id = responseId,
                status = fromValue(responseStatus),
                type = ResponseType.Json,
                contentTypeHeader = "application/json".takeIf { responseId != null },
                headers = headers,
            )
        }
    }

    private fun combinedParameters(
        key: String,
        path: PathItem,
        second: OpenAPI,
        op: PathItem.() -> Operation?,
    ): List<Parameter> {
        val pathParam: List<Parameter> = path.parameters ?: emptyList()
        val opParam: List<Parameter> = path.op()?.parameters ?: emptyList()
        val refPathParam: List<Parameter> = second.paths[key]?.parameters ?: emptyList()
        val refOpParam: List<Parameter> = second.paths[key]?.op()?.parameters ?: emptyList()
        return pathParam + opParam + refPathParam + refOpParam
    }

    private fun parseEndpoints(openApi: OpenAPI, second: OpenAPI) = buildList {
        openApi.paths.forEach { (key, path) ->
            path.get?.let {
                this += generateEndpoint(key, HttpMethod.Get, it, combinedParameters(key, path, second) { get })
            }
            path.post?.let {
                this += generateEndpoint(key, HttpMethod.Post, it, combinedParameters(key, path, second) { post })
            }
            path.delete?.let {
                this += generateEndpoint(key, HttpMethod.Delete, it, combinedParameters(key, path, second) { delete })
            }
            path.patch?.let {
                this += generateEndpoint(key, HttpMethod.Patch, it, combinedParameters(key, path, second) { patch })
            }
            path.put?.let {
                this += generateEndpoint(key, HttpMethod.Put, it, combinedParameters(key, path, second) { put })
            }
            path.head?.let {
                this += generateEndpoint(key, HttpMethod.Head, it, combinedParameters(key, path, second) { head })
            }
        }
    }

    private fun parseSpecs(yamlFile: File, endpoints: List<Endpoint>): Map<String, JsonSpecFile> {
        val rootPath = yamlFile.parent + File.separator
        val jsonParser = JsonParser(log, File(rootPath))

        fun String.normalize(): String {
//            var path = Path(this).normalize().pathString
//            val file = File(rootPath, path)
//            require(file.isFile && file.exists()) { "Cannot locate referenced file ${file.absolutePath}" }
//            if (path.startsWith("./").not()) {
//                path = "./$path"
//            }
//            return path

            return this
        }

        val queue = mutableListOf<String>().apply {
            addAll(endpoints.mapNotNull { it.response.id?.normalize() })
            addAll(endpoints.mapNotNull { it.bodyId?.normalize() })
            addAll(endpoints.flatMap { it.errorResponseIds.map { error -> error.value.normalize() } })
        }

        val processedFiles = mutableSetOf<String>()

        return buildMap {
            while (queue.isNotEmpty()) {
                val path = queue.removeFirst()
                if (processedFiles.contains(path)) continue
                processedFiles.add(path)
                val result = jsonParser.parse(File(rootPath, path))
                val newSchemas = result.imports
                    .map { it.path.normalize() }
                    .filterNot { processedFiles.contains(it) }
                queue.addAll(newSchemas)

                put(path, result)
            }
        }
    }

    fun parse(file: File): SwaggerSpec {
        // https://github.com/swagger-api/swagger-parser

        fun load(isResolve: Boolean): SwaggerParseResult {
            val options = ParseOptions().apply { this.isResolve = isResolve }
            return OpenAPIParser().readLocation(file.absolutePath, null, options)
        }

        val openApi: OpenAPI = load(false).openAPI
        val endpoints: List<Endpoint> = parseEndpoints(openApi, load(true).openAPI)

        log.d("Found ${endpoints.size} endpoints:")
        endpoints.forEach { endpoint ->
            val path = endpoint.path.joinToString("/")
            val space = "".padStart(path.length)
            val success = endpoint.response.status.value
            log.d("- $path |- ${endpoint.method.value.padEnd(4)} ${endpoint.bodyId ?: ""}")
            log.d("  $space |  $success ${endpoint.response.id ?: ""}")
            endpoint.errorResponseIds.forEach { (code, errorId) ->
                log.d("  $space |  ${code.value} $errorId")
            }
        }

        val specs = parseSpecs(file, endpoints)

        log.d("Total ${specs.size} JSON schema files")
        specs.forEach { (path, spec) ->
            log.d("- $path -> ${spec.model.smartToString()}")
            if (spec.model is SealedJsonType) {
                spec.model.types.forEach { type ->
                    log.d("  |- ${type.smartToString()}")
                }
            }
            spec.definitions.forEach { (name, def) ->
                log.d("  |- $name -> ${def.smartToString()}")
            }
        }
        return SwaggerSpec(endpoints, specs)
    }
}
