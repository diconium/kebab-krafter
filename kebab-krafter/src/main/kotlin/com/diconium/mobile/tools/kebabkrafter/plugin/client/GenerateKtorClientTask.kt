package com.diconium.mobile.tools.kebabkrafter.plugin.client

import com.diconium.mobile.tools.kebabkrafter.generator.ktorclient.generateKtorClientFor
import com.diconium.mobile.tools.kebabkrafter.named
import com.diconium.mobile.tools.kebabkrafter.plugin.TransformerSpec
import com.diconium.mobile.tools.kebabkrafter.plugin.buildTransformers
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
abstract class GenerateKtorClientTask : DefaultTask() {

    @get:Console
    abstract val log: Property<Boolean>

    @get:Input
    abstract val clientName: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val specFile: Property<File>

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    @get:Nested
    abstract val transformerSpec: TransformerSpec

    @TaskAction
    fun action() {
        generateKtorClientFor(
            name = clientName.get(),
            log = logger.named(clientName.get()),
            packageName = packageName.get(),
            baseDir = outputFolder.get().asFile,
            specFile = specFile.get(),
            transformers = transformerSpec.buildTransformers(),
        )
    }
}
