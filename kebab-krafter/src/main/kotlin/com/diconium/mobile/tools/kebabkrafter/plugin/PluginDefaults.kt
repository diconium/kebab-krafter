package com.diconium.mobile.tools.kebabkrafter.plugin

import com.diconium.mobile.tools.kebabkrafter.EndpointTransformer
import com.diconium.mobile.tools.kebabkrafter.KtorController
import com.diconium.mobile.tools.kebabkrafter.KtorTransformer
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

//region default transformers
internal class DefaultEndpointTransformer : EndpointTransformer {
    override fun transform(endpoint: Endpoint) = endpoint
}

internal class DefaultKtorTransformer : KtorTransformer {
    override fun transform(endpoint: Endpoint, controller: KtorController) = controller
}
//endregion

//region those are copied from those auto-generated accessors files,
// just to make the usage above a bit cleaner.
private fun Project.sourceSets(configure: Action<SourceSetContainer>): Unit =
    (this as ExtensionAware).extensions.configure("sourceSets", configure)

private val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = named("main")

private val SourceSet.kotlin: SourceDirectorySet
    get() = (this as ExtensionAware).extensions.getByName("kotlin")
        as SourceDirectorySet

//endregion
internal fun Project.registerTask(task: Any) {
    this.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        this.sourceSets { container ->
            container.main.configure { sourceSet ->
                sourceSet.java.srcDirs(task)
                sourceSet.kotlin.srcDirs(task)
            }
        }
    }
}
