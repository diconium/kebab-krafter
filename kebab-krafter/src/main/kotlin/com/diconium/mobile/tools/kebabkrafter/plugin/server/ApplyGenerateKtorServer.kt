package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.generator.DefaultKtorControllerMapper
import com.diconium.mobile.tools.kebabkrafter.generator.EndpointTransformer
import com.diconium.mobile.tools.kebabkrafter.generator.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.KtorTransformer
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

fun applyGenerateKtorServer(target: Project) {

    // create extension
    val ktorServerInput = target.extensions.create("ktorServer", KtorServerExtension::class.java)

    // apply defaults
    ktorServerInput.log.convention(false)
    ktorServerInput.outputFolder.convention(target.defaultOutput)
    ktorServerInput.transformerSpec.endpointTransformer.convention(DefaultEndpointTransformer::class.java)
    ktorServerInput.transformerSpec.ktorMapper.convention(DefaultKtorControllerMapper::class.java)
    ktorServerInput.transformerSpec.ktorTransformer.convention(DefaultKtorTransformer::class.java)

    // register task
    val task = target.tasks.register("generateKtorServer", GenerateKtorServerTask::class.java) {
        it.group = "generator"
        it.ktorServerInput.set(ktorServerInput)
    }

    // wire task output to the main source set
    target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
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


private class DefaultEndpointTransformer : EndpointTransformer {
    override fun transform(endpoint: Endpoint) = endpoint
}

private class DefaultKtorTransformer : KtorTransformer {
    override fun transform(endpoint: Endpoint, controller: KtorController) = controller
}

private val Project.defaultOutput: Provider<Directory>
    get() = this.layout.buildDirectory.dir("generated/sources/ktorServer/")
