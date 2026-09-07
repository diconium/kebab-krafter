package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.KebabLogger
import com.diconium.mobile.tools.kebabkrafter.Transformers
import com.diconium.mobile.tools.kebabkrafter.generator.dataclasses.DataClassesGenerator
import com.diconium.mobile.tools.kebabkrafter.generator.logName
import com.diconium.mobile.tools.kebabkrafter.generator.preParseGradleInputs
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
    val inputs = preParseGradleInputs(log, packageName, baseDir, specFile, transformers)
    val dataSpecs = inputs.dataSpecs
    val controllers = inputs.controllers.map { it.copy(packageName = "controllers.${it.packageName}") }

    //region Generate Kotlin code
    log.l("Generating data class models for KtorServer")
    DataClassesGenerator(
        log = log,
        outputDirectory = baseDir,
        basePackageName = packageName,
        dataSpecsMap = dataSpecs,
    ).generate()

    log.l("Generating ${controllers.size} KtorControllers")
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
