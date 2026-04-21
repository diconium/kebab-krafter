package com.diconium.mobile.tools.kebabkrafter.generator.dataclasses

import com.diconium.mobile.tools.kebabkrafter.KebabLogger
import com.diconium.mobile.tools.kebabkrafter.models.JsonSpecFile
import java.io.File

internal class DataClassesGenerator(
    private val log: KebabLogger,
    private val outputDirectory: File,
    private val basePackageName: String,
    private val dataSpecsMap: Map<String, JsonSpecFile>,
) {

    fun generate() = dataSpecsMap.forEach { (path, spec) ->
        DataClassGenerator(log, outputDirectory, basePackageName, path, spec).generate()
    }
}
