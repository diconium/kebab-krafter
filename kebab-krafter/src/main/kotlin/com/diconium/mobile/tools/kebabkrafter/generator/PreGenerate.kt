package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import com.diconium.mobile.tools.kebabkrafter.parser.SwaggerParser
import java.io.File

/**
 * re-use between ktor server and client
 */
internal fun preGenerate(
	packageName: String,
	baseDir: File,
	specFile: File,
	transformers: Transformers,
): PreGenerated {
	// clean the output folder
	File(baseDir, packageName.replace(".", "/")).apply {
		deleteRecursively()
		mkdirs()
	}

	// parse the specification
	var spec = SwaggerParser.parse("10.0.0.1", specFile)

	//region map and transforms the input
	Log.d("Transforming endpoints:")
	spec = spec.copy(
		endpoints = spec.endpoints.map { endpoint ->
			val before = endpoint.logName
			transformers.endpointTransformer.transform(endpoint)
				.also {
					val after = it.logName
					if (before != after) Log.d("- $after")
				}
		},
	)

	val shortestPath = spec.endpoints.minByOrNull { it.path.size }!!.path.size
	Log.d("Shortest path length is: $shortestPath")

	Log.d("Mapping endpoints to ktorControllers:")
	var controllers = spec.endpoints.map { endpoint ->
		transformers.ktorMapper.map(shortestPath, spec.dataSpecs, endpoint)
			.also { Log.d("- ${endpoint.logName} -> ${it.logName}") }
	}

	Log.d("Transforming KtorControllers:")
	controllers = controllers.map { ctrl ->
		val before = ctrl.logName
		transformers.ktorTransformer.transform(ctrl)
			.also {
				val after = it.logName
				if (before != after) Log.d("- $after")
			}
	}
	//endregion

	return PreGenerated(spec.dataSpecs, controllers)
}

data class PreGenerated(
	val dataSpecs: Map<String, BaseSpecModel>,
	val controllers: List<KtorController>
)

val KtorController.logName: String
	get() = "$ktorFunction($route)[$packageName.$className]"

val Endpoint.logName: String
	get() = "${method.value}/ ${path.joinToString("/")}"
