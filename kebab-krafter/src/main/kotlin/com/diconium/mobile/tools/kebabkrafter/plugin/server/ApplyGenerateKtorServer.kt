package com.diconium.mobile.tools.kebabkrafter.plugin.server

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

fun applyGenerateKtorServer(target: Project) {
    val ktorServerInput = target.extensions.create("ktorServer", KtorServerExtension::class.java)

    val defaultOutput = target.layout.buildDirectory.dir("generated/sources/ktorServer/")
    ktorServerInput.outputFolder.convention(defaultOutput)

    val task = target.tasks.register("generateKtorServer", GenerateKtorServerTask::class.java) {
        it.group = "generator"
        it.ktorServerInput.set(ktorServerInput)
    }

    target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        target.tasks.named("compileKotlin").configure {
            it.dependsOn(task)
        }

        target.sourceSets { container ->
            container.main.configure { sourceSet ->
                sourceSet.java.srcDirs(task)
                sourceSet.kotlin.srcDirs(task)
            }
        }
    }
}

// those are copied from those auto-generated accessors files,
// just to make the usage above a bit cleaner.
private fun Project.sourceSets(configure: Action<SourceSetContainer>): Unit =
    (this as ExtensionAware).extensions.configure("sourceSets", configure)

private val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = named("main")

private val SourceSet.kotlin: SourceDirectorySet
    get() = (this as ExtensionAware).extensions.getByName("kotlin")
        as SourceDirectorySet


