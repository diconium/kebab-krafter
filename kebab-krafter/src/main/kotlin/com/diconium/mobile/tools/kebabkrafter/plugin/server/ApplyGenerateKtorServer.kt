package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.DefaultKtorControllerMapper
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.EndpointTransformer
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.KtorTransformer
import com.diconium.mobile.tools.kebabkrafter.generator.toCamelCase
import com.diconium.mobile.tools.kebabkrafter.generator.toPascalCase
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

fun applyGenerateKtorServer(target: Project) {
    // create extension
    val ktorServer = target.extensions.create("ktorServer", KtorServerExtension::class.java)

    // apply defaults
    ktorServer.log.convention(false)

    val baseTask = target.tasks.register("generateKtorServer", DefaultTask::class.java) {
        it.group = "generator"
    }

    ktorServer.services.whenObjectAdded { ktorServerInput ->

        require(ktorServerInput.name.isBlank().not()) {
            "Service name cannot be empty, use `default{}` instead"
        }
        val folderName = ktorServerInput.name.toCamelCase()
        val output = target.layout.buildDirectory.dir("generated/sources/ktorServer/$folderName/")
        ktorServerInput.outputFolder.convention(output)
        ktorServerInput.transformerSpec.endpointTransformer.convention(DefaultEndpointTransformer::class.java)
        ktorServerInput.transformerSpec.ktorMapper.convention(DefaultKtorControllerMapper::class.java)
        ktorServerInput.transformerSpec.ktorTransformer.convention(DefaultKtorTransformer::class.java)
        ktorServerInput.installFunction.convention("install${ktorServerInput.name.toPascalCase()}GeneratedRoutes")

        // register task(s)
        val taskName = "generate${ktorServerInput.name.toPascalCase()}KtorServer"
        val task = target.tasks.register(taskName, GenerateKtorServerTask::class.java) {
            // it.group = "generator"
            it.ktorServerInput.set(ktorServerInput)
            it.log.set(ktorServer.log)
        }
        baseTask.configure { it.dependsOn(task) }

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
