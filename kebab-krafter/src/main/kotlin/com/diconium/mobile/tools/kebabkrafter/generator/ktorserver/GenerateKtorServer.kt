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

    //region Generate Kotlin code
    Log.d("Generating data class models:")
    DataClassesGenerator(
        basePackageName = packageName,
        spec.dataSpecs,
        outputDirectory = baseDir,
    ).generateDataModelFiles()

    Log.d("Generating KtorControllers:")
    val ctrlGenerator = KtorControllerGenerator(
        basePackage = packageName,
        context = contextSpec.asClassName(),
    )
    controllers.forEach { ctrl ->
        Log.d("- ${ctrl.logName}")
        ctrlGenerator.generate(ctrl).writeTo(baseDir)
    }

    Log.d("Generating fun Routes.$installFunction():")
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
