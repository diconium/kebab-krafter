package com.diconium.mobile.tools.kebabkrafter.plugin.client

import com.diconium.mobile.tools.kebabkrafter.DefaultKtorControllerMapper
import com.diconium.mobile.tools.kebabkrafter.generator.toCamelCase
import com.diconium.mobile.tools.kebabkrafter.generator.toPascalCase
import com.diconium.mobile.tools.kebabkrafter.plugin.DefaultEndpointTransformer
import com.diconium.mobile.tools.kebabkrafter.plugin.DefaultKtorTransformer
import com.diconium.mobile.tools.kebabkrafter.plugin.registerAndroid
import com.diconium.mobile.tools.kebabkrafter.plugin.registerTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project

fun applyGenerateKtorClient(target: Project) {
    // create extension
    val ktorClient = target.extensions.create("ktorClient", KtorClientExtension::class.java)

    // apply defaults
    ktorClient.log.convention(false)

    val baseTask by lazy {
        target.tasks.register("generateKtorClient", DefaultTask::class.java) {
            it.group = "generator"
        }
    }

    ktorClient.services.whenObjectAdded { ktorClientInput ->
        require(ktorClientInput.name.isBlank().not()) { "Service name cannot be empty" }
        val folderName = ktorClientInput.name.toCamelCase()
        val output = target.layout.buildDirectory.dir("generated/sources/ktorClient/$folderName/")
        ktorClientInput.outputFolder.convention(output)
        ktorClientInput.transformerSpec.endpointTransformer.convention(DefaultEndpointTransformer::class.java)
        ktorClientInput.transformerSpec.ktorMapper.convention(DefaultKtorControllerMapper::class.java)
        ktorClientInput.transformerSpec.ktorTransformer.convention(DefaultKtorTransformer::class.java)

        // register task(s)
        val taskName = "generate${ktorClientInput.name.toPascalCase()}KtorClient"
        val task = target.tasks.register(taskName, GenerateKtorClientTask::class.java) {
            it.log.set(ktorClient.log)
            it.clientName.set(ktorClientInput.name)
            it.packageName.set(ktorClientInput.packageName)
            it.specFile.set(ktorClientInput.specFile)
            it.outputFolder.set(ktorClientInput.outputFolder)
            it.transformerSpec.endpointTransformer.set(ktorClientInput.transformerSpec.endpointTransformer)
            it.transformerSpec.ktorMapper.set(ktorClientInput.transformerSpec.ktorMapper)
            it.transformerSpec.ktorTransformer.set(ktorClientInput.transformerSpec.ktorTransformer)
        }
        baseTask.configure { it.dependsOn(task) }

        // wire task output to the main source set
        target.registerTask(task)
        target.registerAndroid(task)
    }
}
