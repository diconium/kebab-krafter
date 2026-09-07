package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.DefaultKtorControllerMapper
import com.diconium.mobile.tools.kebabkrafter.generator.toCamelCase
import com.diconium.mobile.tools.kebabkrafter.generator.toPascalCase
import com.diconium.mobile.tools.kebabkrafter.plugin.DefaultEndpointTransformer
import com.diconium.mobile.tools.kebabkrafter.plugin.DefaultKtorTransformer
import com.diconium.mobile.tools.kebabkrafter.plugin.registerTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project

fun applyGenerateKtorServer(target: Project) {
    // create extension
    val ktorServer = target.extensions.create("ktorServer", KtorServerExtension::class.java)

    // apply defaults
    ktorServer.log.convention(false)

    val baseTask = target.tasks.register("generateKtorServer", DefaultTask::class.java) {
        it.group = "generator"
    }

    val serviceLocatorTask = target.tasks.register(
        "generateKtorServerServiceLocator",
        KtorServerServiceLocatorTask::class.java,
    ) { task ->
        val output = target.layout.buildDirectory.dir("generated/sources/ktorServer/serviceLocator/")
        task.outputFolder.convention(output)
    }

    // wire task output to the main source set
    target.registerTask(serviceLocatorTask)

    ktorServer.services.whenObjectAdded { ktorServerInput ->

        require(ktorServerInput.name.isBlank().not()) { "Service name cannot be empty" }
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
            it.dependsOn(serviceLocatorTask)
        }
        baseTask.configure { it.dependsOn(task) }

        // wire task output to the main source set
        target.registerTask(task)
    }
}
