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

                    // implementation notes:
                    //
                    // That's a very cheeky piece of code that I'm still unsure if genius or stupid.
                    // Gradle tasks uses input/outputs to define UP-TO-DATE information,
                    // and those inputs/outputs must be some type serializable
                    // but the transformers/mappers are lambas (`fun interface`) and that was my problem.
                    // The workaround here is that we set those inputs to `Class<out TYPE>` that are serializable.
                    // in the extension object we capture the anonymous inner class from the lambda
                    // and here we build a new instance of that lambda.
                    //
                    // This seems to work fine for simple lambdas, but, I can imagine on more complex scenario,
                    // (e.g. if a lambda access components from the encompassing `Project`)
                    // that it might not work so good, or produce unknown side effects.
                    //
                    // I think it's good that the transformers API is wrapped in an OptIn(KebabKrafterUnstableApi)

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
