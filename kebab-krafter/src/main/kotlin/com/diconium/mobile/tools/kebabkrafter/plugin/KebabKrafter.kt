package com.diconium.mobile.tools.kebabkrafter.plugin

import com.diconium.mobile.tools.kebabkrafter.plugin.client.GenerateKtorClient
import com.diconium.mobile.tools.kebabkrafter.plugin.server.GenerateKtorServer
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project

class KebabKrafter : Plugin<Project> {
	override fun apply(target: Project) {
		GenerateKtorServer.apply(target)
		GenerateKtorClient.apply(target)
	}

	companion object {
		internal const val TASK_GROUP = "kebab krafter"
	}
}

// those are copied from those auto-generated accessors files,
// just to make the usage above a bit cleaner.
internal fun Project.sourceSets(configure: Action<org.gradle.api.tasks.SourceSetContainer>): Unit =
	(this as org.gradle.api.plugins.ExtensionAware).extensions.configure("sourceSets", configure)

internal val org.gradle.api.tasks.SourceSetContainer.main: NamedDomainObjectProvider<org.gradle.api.tasks.SourceSet>
	get() = named("main")

internal val org.gradle.api.tasks.SourceSet.kotlin: org.gradle.api.file.SourceDirectorySet
	get() = (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin")
		as org.gradle.api.file.SourceDirectorySet
