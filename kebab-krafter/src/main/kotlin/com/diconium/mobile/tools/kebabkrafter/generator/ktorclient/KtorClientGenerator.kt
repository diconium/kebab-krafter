package com.diconium.mobile.tools.kebabkrafter.generator.ktorclient

import com.diconium.mobile.tools.kebabkrafter.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.io.File

class KtorClientGenerator(
	private val basePackageName: String,
	private val name: String,
	private val swaggerSpec: SwaggerSpec,
	private val outputDirectory: File,
) {

}



//{
//	private val dataModelGenerator = DataClassesGenerator(basePackageName, swaggerSpec.dataSpecs, outputDirectory)
//
//	fun generate() {
//		dataModelGenerator.generateDataModelFiles()
//
//		val clientServiceFileSpec = generateHttpClientService()
//		clientServiceFileSpec.writeTo(outputDirectory)
//	}
//
//	private fun generateHttpClientService(): FileSpec {
//		val className = ClassName(basePackageName, name)
//
//		return FileSpec.builder(className)
//			.addType(
//				// Class
//				TypeSpec.classBuilder(className).apply {
//					// Modifiers
//					modifiers.run {
//						add(KModifier.PUBLIC)
//					}
//
//					// Constructor
//					primaryConstructor(
//						FunSpec.constructorBuilder()
//							.addParameter("client", HttpClient::class)
//							.build(),
//					)
//
//					// Properties
//					addProperty(
//						PropertySpec.builder("client", HttpClient::class, KModifier.PRIVATE)
//							.initializer("client")
//							.build(),
//					)
//
//					// Methods
//					swaggerSpec.endpoints.forEach { endpoint ->
//						addFunction(generateEndpointMethod(endpoint))
//					}
//				}.build(),
//			).build()
//	}
//
//	private fun String.capitalize(): String {
//		return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
//	}
//
//	private fun generateEndpointMethod(endpoint: Endpoint): FunSpec {
//		// Response
//		val responseSpec: BaseSpecModel = swaggerSpec.dataSpecs[endpoint.response.id]!!
//		val responseClassName = responseSpec.let { "${it.getFullPackageName(basePackageName)}.${it.name}" }
//
//		val method = endpoint.method.value
//
//		//  Function Name
//		val builder = FunSpec.builder(
//			// "${method.lowercase()}${endpoint.path.last { !it.startsWith("{") }.capitalize() }" // Alt Naming Scheme
//			endpoint.path.last { !it.startsWith("{") },
//		).apply {
//			// Return type
//			responseClassName?.let { returns(ClassName.bestGuess(responseClassName)) }
//
//			// Function Modifiers
//			addModifiers(KModifier.SUSPEND)
//
//			// Function Parameters
//			val params = endpoint.queryParameters + endpoint.pathParameters
//			params.forEach { (name, urlType) ->
//				addParameter(ParameterSpec(name, urlType.format.toTypeName()))
//			}
//			responseSpec?.let {
//				addParameter(ParameterSpec("requestBody", it.getClassName(basePackageName)))
//			}
//
//			// Function Body
//			addCode(
//				CodeBlock.builder()
//					.apply {
//						// Request Builder Start
//						beginControlFlow("val response = client.%M", MemberName("io.ktor.client.request", "request"))
//
//						// Set HTTP Method
//						addStatement("method = %M(\"$method\")", MemberName("io.ktor.http", "HttpMethod"))
//
//						// Url Builder Start
//						beginControlFlow("url")
//
//						// Path Segments
//						addStatement(
//							"%M(${endpoint.generatePathSegmentsArgs()})\n",
//							MemberName("io.ktor.http", "appendPathSegments"),
//						)
//
//						// Query Parameters
//						endpoint.queryParameters.forEach { (name, _) ->
//							addStatement("parameters.append(\"$name\", $name.toString())")
//						}
//
//						// Url Builder End
//						endControlFlow()
//
//						// Set Request Body
//						responseSpec?.let {
//							addStatement("%M(requestBody)", MemberName("io.ktor.client.request", "setBody"))
//						}
//
//						// Request Builder End
//						endControlFlow()
//
//						// Return Statement
//						responseClassName?.let {
//							addStatement("return response.%M()", MemberName("io.ktor.client.call", "body"))
//						}
//					}
//					.build(),
//			)
//		}
//
//		return builder.build()
//	}
//
//	private fun Endpoint.generatePathSegmentsArgs(): String {
//		return path.joinToString(", ") {
//			if (it.startsWith("{") && it.endsWith("}")) { // Path parameter. Use parameter name
//				"${it.substring(1, it.length - 1)}.toString()"
//			} else { // Regular URL segment
//				"\"$it\""
//			}
//		}
//	}
//
//	/**
//	 * Convert [UrlType.Format] to corresponding [TypeName].
//	 */
//	private fun UrlType.Format.toTypeName(): TypeName = when (this) {
//		UrlType.Format.Boolean -> Boolean::class.asTypeName()
//		UrlType.Format.Float -> Float::class.asTypeName()
//		UrlType.Format.Int -> Int::class.asTypeName()
//		UrlType.Format.String -> String::class.asTypeName()
//		UrlType.Format.StringArray -> List::class.asTypeName().parameterizedBy(String::class.asTypeName())
//	}
//}
//
//fun main() {
//	val models = listOf(
//		SpecModel(
//			"1",
//			"",
//			"models",
//			"Model1",
//			listOf(SpecField("id", SpecField.Type.Int, true, ""), SpecField("name", SpecField.Type.String, false, "")),
//		),
//		SpecModel(
//			"2",
//			"",
//			"api.models",
//			"Model2",
//			listOf(
//				SpecField("price", SpecField.Type.Float, true, ""),
//				SpecField("isExpired", SpecField.Type.Boolean, true, ""),
//			),
//		),
//		SpecModel(
//			"3",
//			"",
//			"api.models",
//			"Model3",
//			listOf(
//				SpecField("id", SpecField.Type.Int, true, ""),
//				SpecField("model", SpecField.Type.DataModel("1"), false, ""),
//			),
//		),
//		SpecModel(
//			"4",
//			"",
//			"models",
//			"Model4",
//			listOf(
//				SpecField("ids", SpecField.Type.DataArray(SpecField.Type.Int), false, ""),
//				SpecField(
//					"models",
//					SpecField.Type.DataArray(SpecField.Type.DataModel("3")),
//					false,
//					"",
//				),
//			),
//		),
//	)
//
//	val endpoints = listOf(
//		Endpoint(
//			listOf("more", "{param1}", "path1", "{param2}"),
//			"",
//			HttpMethod.Get,
//			Response(models[0].id, HttpStatusCode.OK, ResponseType.Json, "application/json", emptyList()),
//			pathParameters = mapOf(
//				"param1" to UrlType(true, UrlType.Format.String),
//				"param2" to UrlType(true, UrlType.Format.Int),
//			),
//			bodyId = models[2].id,
//		),
//		Endpoint(
//			listOf("more", "path2"),
//			"",
//			HttpMethod.Post,
//			Response(models[1].id, HttpStatusCode.OK, ResponseType.Json, "application/json", emptyList()),
//			mapOf(
//				"param1" to UrlType(true, UrlType.Format.String),
//				"param2" to UrlType(true, UrlType.Format.Boolean),
//				"param3" to UrlType(false, UrlType.Format.Int),
//			),
//			bodyId = models[3].id,
//		),
//	)
//	val swaggerSpec = SwaggerSpec("", endpoints, models.associateBy { it.id })
//	val outputDirectory = File("./src/gen/kotlin/").apply {
//		deleteRecursively()
//		mkdirs()
//	}
//	val generator = KtorClientGenerator("com.example", "SampleClient", swaggerSpec, outputDirectory)
//	generator.generate()
//}
//
//fun listToMap(models: List<SpecModel>): Map<String, SpecModel> {
//	val modelMap = mutableMapOf<String, SpecModel>()
//
//	for (model in models) {
//		val key = "${model.ref}/${model.name}"
//		modelMap[key] = model
//	}
//
//	return modelMap
//}
