package com.diconium.mobile.tools.kebabkrafter.generator.ktorclient

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.generator.Transformers
import com.diconium.mobile.tools.kebabkrafter.generator.dataclasses.DataClassesGenerator
import com.diconium.mobile.tools.kebabkrafter.generator.logName
import com.diconium.mobile.tools.kebabkrafter.generator.preGenerate
import java.io.File

fun generateKtorClientFor(
	packageName: String,
	baseDir: File,
	specFile: File,
	transformers: Transformers = Transformers(),
) {
	val (dataSpecs, controllers) = preGenerate(packageName, baseDir, specFile, transformers)

	Log.d("Generating data class models:")
	DataClassesGenerator(
		basePackageName = packageName,
		dataSpecsMap = dataSpecs,
		outputDirectory = baseDir,
	).generateDataModelFiles()

	Log.d("Generating KtorClientUseCases:")
	val ctrlGenerator = KtorClientUseCasesGenerator(
		basePackage = packageName,
	)
	controllers.forEach { ctrl ->
		Log.d("- ${ctrl.logName}")
		ctrlGenerator.generate(ctrl).writeTo(baseDir)
	}

}
