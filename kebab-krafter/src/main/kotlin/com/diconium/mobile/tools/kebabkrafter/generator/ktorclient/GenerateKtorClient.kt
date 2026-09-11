package com.diconium.mobile.tools.kebabkrafter.generator.ktorclient

import com.diconium.mobile.tools.kebabkrafter.KebabLogger
import com.diconium.mobile.tools.kebabkrafter.Transformers
import com.diconium.mobile.tools.kebabkrafter.generator.dataclasses.DataClassesGenerator
import com.diconium.mobile.tools.kebabkrafter.generator.preParseGradleInputs
import java.io.File

internal fun generateKtorClientFor(
    name: String,
    log: KebabLogger,
    packageName: String,
    parcelable: Boolean,
    baseDir: File,
    specFile: File,
    transformers: Transformers,
) {
    val inputs1 = preParseGradleInputs(log, packageName, baseDir, specFile, transformers)
    val dataSpecs = inputs1.dataSpecs
    val controllers = inputs1.controllers.map { it.copy(packageName = "services.${it.packageName}") }

    log.l("Generating data class models for KtorClient")
    DataClassesGenerator(
        log = log,
        parcelable = parcelable,
        outputDirectory = baseDir,
        basePackageName = packageName,
        dataSpecsMap = dataSpecs,
    ).generate()

    log.l("Generating ${controllers.size} KtorClient $name interfaces")
    val generator = KtorClientGenerator(log, true, packageName)
    controllers.forEach { ctrl ->
        generator.generate(ctrl).writeTo(baseDir)
    }
}
