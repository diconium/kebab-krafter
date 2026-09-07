package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.KebabLogger
import com.diconium.mobile.tools.kebabkrafter.KtorController
import com.diconium.mobile.tools.kebabkrafter.Transformers
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import com.diconium.mobile.tools.kebabkrafter.models.JsonSpecFile
import com.diconium.mobile.tools.kebabkrafter.parser.SwaggerParser
import java.io.File

internal data class ParsedInputs(val dataSpecs: Map<String, JsonSpecFile>, val controllers: List<KtorController>)

internal fun preParseGradleInputs(
    log: KebabLogger,
    packageName: String,
    baseDir: File,
    specFile: File,
    transformers: Transformers,
): ParsedInputs {
    // clean the output folder
    File(baseDir, packageName.replace(".", "/")).apply {
        deleteRecursively()
        mkdirs()
    }

    // parse the specification
    log.l("Parsing ${specFile.name}")
    var spec = SwaggerParser(log).parse(specFile)
    log.l("Found ${spec.endpoints.size} endpoints with ${spec.dataSpecs.size} data models")

    // map and transforms the input
    log.d("Transforming Endpoints")
    spec = spec.copy(
        endpoints = spec.endpoints.map { endpoint ->
            val before = endpoint.logName
            transformers.endpointTransformer.transform(endpoint)
                .also {
                    val after = it.logName
                    if (before != after) log.d("- $after")
                }
        },
    )

    val shortestPath = spec.endpoints.minByOrNull { it.path.size }!!.path.size
    log.d("Shortest path length is: $shortestPath")

    log.d("Mapping Endpoints to ktorControllers")
    val initialControllers = spec.endpoints.map { endpoint ->
        endpoint to transformers.ktorMapper.map(shortestPath, endpoint, spec.dataSpecs)
            .also { log.d("- ${endpoint.logName} -> ${it.logName}") }
    }

    log.d("Transforming KtorControllers")
    val controllers = initialControllers.map { (endpoint, ctrl) ->
        val before = ctrl.logName
        transformers.ktorTransformer.transform(endpoint, ctrl)
            .also {
                val after = it.logName
                if (before != after) log.d("- $after")
            }
    }

    // all ready for the generators
    return ParsedInputs(spec.dataSpecs, controllers)
}

internal val KtorController.logName: String
    get() = "$ktorFunction(${path.joinToString("/")})[$packageName.$className]"

internal val Endpoint.logName: String
    get() = "${method.value}/ ${path.joinToString("/")}"
