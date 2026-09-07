package com.diconium.mobile.tools.kebabkrafter.plugin.client

import com.diconium.mobile.tools.kebabkrafter.generator.ktorclient.generateKtorClientFor
import com.diconium.mobile.tools.kebabkrafter.named
import com.diconium.mobile.tools.kebabkrafter.plugin.buildTransformers
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateKtorClientTask : DefaultTask() {

    @get:Console
    abstract val log: Property<Boolean>

    @get:Nested
    abstract val ktorClientInput: Property<KtorClientServiceExtension>

    @TaskAction
    fun action() {
        with(ktorClientInput.get()) {
            generateKtorClientFor(
                name = name,
                log = logger.named(name),
                packageName = packageName.get(),
                baseDir = outputFolder.get().asFile,
                specFile = specFile.get(),
                transformers = transformerSpec.buildTransformers(),
            )
        }
    }
}
