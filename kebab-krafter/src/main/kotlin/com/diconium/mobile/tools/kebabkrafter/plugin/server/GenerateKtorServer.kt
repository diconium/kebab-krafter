package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.ContextSpec
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.generateKtorServerFor
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import java.io.File

object GenerateKtorServer {
    fun apply(target: Project) {
        val ktorServerInput = target.extensions.create("ktorServer", KtorServerExtension::class.java)

        target.afterEvaluate {
            if (ktorServerInput.packageName.isPresent.not() ||
                ktorServerInput.specFile.isPresent.not() ||
                ktorServerInput.contextSpec.packageName.isPresent.not() ||
                ktorServerInput.contextSpec.className.isPresent.not() ||
                ktorServerInput.contextSpec.factoryName.isPresent.not()
            ) {
                return@afterEvaluate
            }

            val sourceFolder = if (ktorServerInput.outputFolder.isPresent) {
                ktorServerInput.outputFolder.get()
            } else {
                File(target.projectDir, "build/generated/sources/ktorServer/").also {
                    target.sourceSets { container ->
                        container.main.configure { sourceSet ->
                            sourceSet.java.srcDirs(it)
                            sourceSet.kotlin.srcDirs(it)
                        }
                    }
                }
            }

            target.task("generateKtorServer") {
                it.group = "generator"
                it.doLast {
                    if (ktorServerInput.log) {
                        Log.logger = target.logger
                    }
                    with(ktorServerInput) {
                        generateKtorServerFor(
                            packageName = packageName.get(),
                            baseDir = sourceFolder,
                            specFile = specFile.get(),
                            contextSpec = with(contextSpec) {
                                ContextSpec(
                                    packageName = packageName.get(),
                                    className = className.get(),
                                    factoryName = factoryName.get(),
                                )
                            },
                            transformers = transformers,
                        )
                    }
                    Log.logger = null
                }
            }
        }
    }
}

// those are copied from those auto-generated accessors files,
// just to make the usage above a bit cleaner.
private fun Project.sourceSets(configure: Action<org.gradle.api.tasks.SourceSetContainer>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("sourceSets", configure)

private val org.gradle.api.tasks.SourceSetContainer.main: NamedDomainObjectProvider<org.gradle.api.tasks.SourceSet>
    get() = named("main")

private val org.gradle.api.tasks.SourceSet.kotlin: org.gradle.api.file.SourceDirectorySet
    get() = (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin")
        as org.gradle.api.file.SourceDirectorySet
