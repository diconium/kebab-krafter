package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.ContextSpec
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.generateKtorServerFor
import com.diconium.mobile.tools.kebabkrafter.named
import com.diconium.mobile.tools.kebabkrafter.plugin.buildTransformers
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class GenerateKtorServerTask : DefaultTask() {

    @get:Console
    abstract val log: Property<Boolean>

    @get:Nested
    abstract val ktorServerInput: Property<KtorServerServiceExtension>

    @TaskAction
    fun action() {
        with(ktorServerInput.get()) {
            generateKtorServerFor(
                log = logger.named(name),
                packageName = packageName.get(),
                baseDir = outputFolder.get().asFile,
                specFile = specFile.get(),
                installFunction = installFunction.get(),
                contextSpec = with(contextSpec) {
                    ContextSpec(
                        packageName = packageName.get(),
                        className = className.get(),
                        factoryName = factoryName.get(),
                    )
                },
                transformers = transformerSpec.buildTransformers(),
            )
        }
    }
}
