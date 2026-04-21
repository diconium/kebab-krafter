package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.KebabLogger
import com.diconium.mobile.tools.kebabkrafter.generator.dataclasses.DataClassesGenerator
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import com.diconium.mobile.tools.kebabkrafter.parser.SwaggerParser
import java.io.File

/**
 * Helper to generate server + data classes together
 */
internal fun generateKtorServerFor(
    log: KebabLogger,
    packageName: String,
    baseDir: File,
    specFile: File,
    contextSpec: ContextSpec,
    transformers: Transformers,
    installFunction: String,
) {
    // clean the output folder
    File(baseDir, packageName.replace(".", "/")).apply {
        deleteRecursively()
        mkdirs()
    }

    // parse the specification
    log.l("Parsing ${specFile.name}")
    var spec = SwaggerParser(log).parse(specFile)
    log.l("Found ${spec.endpoints.size} endpoints with ${spec.dataSpecs.size} data models")

    //region map and transforms the input
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
    //endregion

    //region Generate Kotlin code
    log.l("Generating data class models")
    DataClassesGenerator(
        log = log,
        outputDirectory = baseDir,
        basePackageName = packageName,
        dataSpecsMap = spec.dataSpecs,
    ).generate()

    log.l("Generating KtorControllers")
    val ctrlGenerator = KtorControllerGenerator(
        basePackage = packageName,
        context = contextSpec.asClassName(),
    )
    controllers.forEach { ctrl ->
        log.d("- ${ctrl.logName}")
        ctrlGenerator.generate(ctrl).writeTo(baseDir)
    }

    log.l("Generating fun Routes.$installFunction()")
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
