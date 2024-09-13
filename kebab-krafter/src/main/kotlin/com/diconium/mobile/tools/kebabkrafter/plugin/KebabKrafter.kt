package com.diconium.mobile.tools.kebabkrafter.plugin

import com.diconium.mobile.tools.kebabkrafter.plugin.server.GenerateKtorServer
import org.gradle.api.Plugin
import org.gradle.api.Project

class KebabKrafter : Plugin<Project> {
	override fun apply(target: Project) {
		GenerateKtorServer.apply(target)
	}
}
