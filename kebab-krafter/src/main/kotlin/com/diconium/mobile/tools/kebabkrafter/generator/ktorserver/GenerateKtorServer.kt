package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.generator.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.Transformers
import com.diconium.mobile.tools.kebabkrafter.generator.dataclasses.DataClassesGenerator
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import com.diconium.mobile.tools.kebabkrafter.parser.SwaggerParser
import java.io.File

/**
 * Helper to generate server + data classes together
 */
fun generateKtorServerFor(
    packageName: String,
    baseDir: File,
    specFile: File,
    contextSpec: ContextSpec,
    transformers: Transformers = Transformers(),
    installFunction: String = "installGeneratedRoutes",
) {
    // clean the output folder
    File(baseDir, packageName.replace(".", "/")).apply {
        deleteRecursively()
        mkdirs()
    }

    // parse the specification
    Log.l("Parsing ${specFile.name}...")
    var spec = SwaggerParser.parse("10.0.0.1", specFile)
    Log.l("Found ${spec.endpoints.size} endpoints with ${spec.dataSpecs.size} data models")

    //region map and transforms the input
    Log.l("Transforming Endpoints")
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

    Log.l("Mapping Endpoints to ktorControllers")
    val initialControllers = spec.endpoints.map { endpoint ->
        endpoint to transformers.ktorMapper.map(shortestPath, spec.dataSpecs, endpoint)
            .also { Log.d("- ${endpoint.logName} -> ${it.logName}") }
    }

    Log.l("Transforming KtorControllers")
    val controllers = initialControllers.map { (endpoint, ctrl) ->
        val before = ctrl.logName
        transformers.ktorTransformer.transform(endpoint, ctrl)
            .also {
                val after = it.logName
                if (before != after) Log.d("- $after")
            }
    }
    //endregion

    //region Generate Kotlin code
    Log.l("Generating data class models")
    DataClassesGenerator(
        basePackageName = packageName,
        spec.dataSpecs,
        outputDirectory = baseDir,
    ).generateDataModelFiles()

    Log.l("Generating KtorControllers")
    val ctrlGenerator = KtorControllerGenerator(
        basePackage = packageName,
        context = contextSpec.asClassName(),
    )
    controllers.forEach { ctrl ->
        Log.d("- ${ctrl.logName}")
        ctrlGenerator.generate(ctrl).writeTo(baseDir)
    }

    Log.l("Generating fun Routes.$installFunction()")
    val routeGenerator = KtorRouteGenerator(
        basePackage = packageName,
        context = contextSpec,
        outputDirectory = baseDir,
    )
    routeGenerator.generate(installFunction, controllers)
    //endregion
}

private val KtorController.logName: String
    get() = "$ktorFunction($route)[$packageName.$className]"

private val Endpoint.logName: String
    get() = "${method.value}/ ${path.joinToString("/")}"
