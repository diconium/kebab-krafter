package com.diconium.mobile.tools.kebabkrafter.parser

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.models.*
import com.diconium.mobile.tools.kebabkrafter.models.SpecField.Type.DataModel
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.fromValue
import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.core.models.SwaggerParseResult
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
	ignoreUnknownKeys = true
	explicitNulls = false
}

object SwaggerParser {

	private fun generateEndpoint(
		key: String,
		method: HttpMethod,
		operation: Operation,
		pathParameters: List<Parameter>?,
	): Endpoint {
		Log.d("Parsing endpoint $key")

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
			response = parseEndpointResponse(operation),
			description = operation.summary,
			bodyId = operation.requestBody?.content?.get("application/json")?.schema?.`$ref`
				?.takeIf { it.endsWith(".json") },
		)
	}

	private fun parseEndpointResponse(operation: Operation): Response {
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
				contentTypeHeader = content?.toList()?.first()?.first!!,
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

	private fun parseSpecs(file: File, endpoints: List<Endpoint>): Map<String, BaseSpecModel> {
		val queue = mutableListOf<String>().apply {
			addAll(endpoints.mapNotNull { it.response.id })
			addAll(endpoints.mapNotNull { it.bodyId })
		}

		val sealedModels = mutableMapOf<String, String>()
		val notAmodel = mutableMapOf<String, SpecField.Type>()
		val wipSpecs = mutableMapOf<String, BaseSpecModel?>()

		while (queue.isNotEmpty()) {
			wipSpecs.computeIfAbsent(queue.removeFirst()) { path ->

				Log.d("Parsing specs $path")

				val subPaths = path.split("#/\$defs/")
				val specFile = File(file.parentFile, subPaths[0])

				val schema = run {
					val schema = json.decodeFromString<JsonSchema>(specFile.readText())
					if (subPaths.size == 1) {
						schema
					} else {
						schema.defs[subPaths[1]]!!
					}
				}

				val className = if (subPaths.size == 1) {
					specFile.nameWithoutExtension
				} else {
					subPaths[1].replaceFirstChar { it.uppercase() }
				}

				(schema.type to null).asType()?.let { type ->
					// sometimes a .json spec might be just to specify the description/pattern and not be a data model
					// here we have just want to remember them and return null
					// in the next step we'll replace them in the spec models back to their real type
					notAmodel[path] = type
					return@computeIfAbsent null
				}

				if (schema.oneOf.isNotEmpty()) {
					return@computeIfAbsent SealedSpecModel(
						id = path,
						description = schema.description,
						relativePackageName = specFile.relativePackageName(file),
						name = specFile.nameWithoutExtension,
						types = schema.oneOf.map { oneOf ->
							val childModel = File(specFile.parentFile, oneOf.ref)
							val childModelId = childModel.relativeTo(file).path.replace("../", "")
							sealedModels[childModelId] = path // mark this as a child of a sealed parent
							queue += childModelId // adds this child to the processing queue
							childModelId
						},
					)
				}

				if (schema.anyOf.isNotEmpty()) {
					return@computeIfAbsent SealedSpecModel(
						id = path,
						description = schema.description,
						relativePackageName = specFile.relativePackageName(file),
						name = specFile.nameWithoutExtension,
						types = schema.anyOf.map { oneOf ->
							val childModel = File(specFile.parentFile, oneOf.ref)
							val childModelId = childModel.relativeTo(file).path.replace("../", "")
							sealedModels[childModelId] = path // mark this as a child of a sealed parent
							queue += childModelId // adds this child to the processing queue
							childModelId
						},
					)
				}

				val fields = mutableListOf<SpecField>()
				schema.properties.forEach { (name, value) ->

					// sealed class discriminator
					value.const?.let { const ->
						fields += SpecField(
							name = name,
							description = value.description,
							type = SpecField.Type.SealedSerializationDiscriminator(const),
							isRequired = schema.required.contains(name),
						)
					}

					// simple type
					value.asType()?.let { type ->
						fields += SpecField(
							name = name,
							description = value.description,
							type = type,
							isRequired = schema.required.contains(name),
						)
					}

					// object type defined on a separate file
					value.ref?.takeUnless { it.startsWith("#") }?.let { ref ->
						val refFile = File(specFile.parentFile, ref)
						val id = refFile.relativeTo(file).path.replace("../", "")
						queue += id
						fields += SpecField(
							name = name,
							description = value.description,
							type = SpecField.Type.DataModel(id),
							isRequired = schema.required.contains(name),
						)
					}

					// object type defined in the same file
					value.ref?.takeIf { it.startsWith("#") }?.let { ref ->

						val id = subPaths[0] + ref
						queue += id

						fields += SpecField(
							name = name,
							description = value.description,
							type = SpecField.Type.DataModel(id),
							isRequired = schema.required.contains(name),
						)
					}

					// array type
					value.items?.takeIf { value.type == "array" }?.let { array ->
						val arrayType = (array.type to array.format).asType()
						when {
							// array of -> simple type
							arrayType != null -> {
								fields += SpecField(
									name = name,
									description = value.description,
									type = SpecField.Type.DataArray(arrayType),
									isRequired = schema.required.contains(name),
								)
							}

							// array of -> object type defined on a separate file
							array.ref != null && array.ref.startsWith("#").not() -> {
								val refFile = File(specFile.parentFile, array.ref)
								val id = refFile.relativeTo(file).path.replace("../", "")
								queue += id
								fields += SpecField(
									name = name,
									description = value.description,
									type = SpecField.Type.DataArray(SpecField.Type.DataModel(id)),
									isRequired = schema.required.contains(name),
								)
							}

							// array of -> object type defined in the same file
							array.ref != null && array.ref.startsWith("#") -> {
								val id = subPaths[0] + array.ref
								queue += id

								fields += SpecField(
									name = name,
									description = value.description,
									type = SpecField.Type.DataArray(SpecField.Type.DataModel(id)),
									isRequired = schema.required.contains(name),
								)
							}
						}
					}
				}

				SpecModel(
					id = path,
					description = schema.description,
					relativePackageName = specFile.relativePackageName(file),
					name = className,
					fields = fields,
					parentId = sealedModels[path],
				)
			}
		}

		val specs = buildMap {
			wipSpecs.forEach { (path, model) ->

				when (model) {
					is SealedSpecModel -> {
						put(path, model)
						Unit
					}

					is SpecModel -> {
						// to build the final output we'll add all the spec model that were not null
						// and map the fields that were mistakenly added as a data model back to their real types
						put(
							path,
							model.copy(
								fields = model.fields.map { specField ->
									if (specField.type is DataModel) {
										notAmodel[specField.type.id]?.let { correctType ->
											specField.copy(type = correctType)
										} ?: specField
									} else {
										specField
									}
								},
							),
						)
						Unit
					}

					null -> {}
				}
			}
		}
		return specs
	}

	fun parse(baseUrl: String, file: File): SwaggerSpec {
		// https://github.com/swagger-api/swagger-parser

		fun load(isResolve: Boolean): SwaggerParseResult {
			val options = ParseOptions().apply { this.isResolve = isResolve }
			return OpenAPIParser().readLocation(file.absolutePath, null, options)
		}

		val openApi: OpenAPI = load(false).openAPI
		val endpoints = parseEndpoints(openApi, load(true).openAPI)
		val specs = parseSpecs(file, endpoints)

		return SwaggerSpec(baseUrl, endpoints, specs)
	}
}

private fun JsonSchema.Item.asType(): SpecField.Type? {
	return when (type) {
		"string" -> when {
			format == "date-time" -> SpecField.Type.Date
			!enum.isNullOrEmpty() -> SpecField.Type.Enum(enum)
			else -> SpecField.Type.String
		}

		"boolean" -> SpecField.Type.Boolean
		"integer" -> SpecField.Type.Int
		"number" -> SpecField.Type.Float
		else -> null
	}
}

private fun Pair<String?, String?>.asType(): SpecField.Type? {
	return when (first) {
		"string" -> when (second) {
			"date-time" -> SpecField.Type.Date
			else -> SpecField.Type.String
		}

		"boolean" -> SpecField.Type.Boolean
		"integer" -> SpecField.Type.Int
		"number" -> SpecField.Type.Float
		else -> null
	}
}

private fun File.relativePackageName(base: File): String {
	return parentFile.relativeTo(base).path
		.replace("../", "")
		.replace("/", ".")
		.replace("schemas", "models")
}
