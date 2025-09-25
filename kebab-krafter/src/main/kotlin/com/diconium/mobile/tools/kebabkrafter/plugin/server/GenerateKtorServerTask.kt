package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.generator.Transformers
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.ContextSpec
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.generateKtorServerFor
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

abstract class GenerateKtorServerTask : DefaultTask() {

    @get:Nested
    abstract val ktorServerInput: Property<KtorServerExtension>

    @TaskAction
    fun action() {
        with(ktorServerInput.get()) {

            generateKtorServerFor(
                packageName = packageName.get(),
                baseDir = outputFolder.get().asFile,
                specFile = specFile.get(),
                contextSpec = with(contextSpec) {
                    ContextSpec(
                        packageName = packageName.get(),
                        className = className.get(),
                        factoryName = factoryName.get(),
                    )
                },

                transformers = with(ktorServerInput.get().transformerSpec) {
                    val et = endpointTransformer.get().getDeclaredConstructor()
                    et.isAccessible = true

                    val km = ktorMapper.get().getDeclaredConstructor()
                    km.isAccessible = true

                    val kt = ktorTransformer.get().getDeclaredConstructor()
                    kt.isAccessible = true

                    Transformers(et.newInstance(), km.newInstance(), kt.newInstance())
                },
            )
        }
    }
}
