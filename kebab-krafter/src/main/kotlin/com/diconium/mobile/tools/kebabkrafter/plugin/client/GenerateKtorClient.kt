package com.diconium.mobile.tools.kebabkrafter.plugin.client

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.generator.ktorclient.generateKtorClientFor
import com.diconium.mobile.tools.kebabkrafter.plugin.KebabKrafter
import com.diconium.mobile.tools.kebabkrafter.plugin.kotlin
import com.diconium.mobile.tools.kebabkrafter.plugin.main
import com.diconium.mobile.tools.kebabkrafter.plugin.server.KtorServerExtension
import com.diconium.mobile.tools.kebabkrafter.plugin.sourceSets
import org.gradle.api.Project
import java.io.File

object GenerateKtorClient {
	fun apply(target: Project) {
		val ktorServerInput = target.extensions.create("ktorClient", KtorServerExtension::class.java)

		target.afterEvaluate {
			if (ktorServerInput.packageName.isPresent.not() ||
				ktorServerInput.specFile.isPresent.not()
			) {
				return@afterEvaluate
			}

			val sourceFolder = File(target.projectDir, "build/generated/sources/ktorClient/")
			target.sourceSets { container ->
				container.main.configure { sourceSet ->
					sourceSet.java.srcDirs(sourceFolder)
					sourceSet.kotlin.srcDirs(sourceFolder)
				}
			}

			target.task("generateKtorClient") {
				it.group = KebabKrafter.TASK_GROUP
				it.doLast {
					if (ktorServerInput.log) {
						Log.logger = target.logger
					}
					with(ktorServerInput) {
						generateKtorClientFor(
							packageName = packageName.get(),
							baseDir = sourceFolder,
							specFile = specFile.get(),
							transformers = transformers,
						)
//						generateKtorServerFor(
//							packageName = packageName.get(),
//							baseDir = sourceFolder,
//							specFile = specFile.get(),
//							contextSpec = with(contextSpec) {
//								ServerContextSpec(
//									packageName = packageName.get(),
//									className = className.get(),
//									factoryName = factoryName.get(),
//								)
//							},
//							transformers = transformers,
//						)
					}
					Log.logger = null
				}
			}
		}
	}
}
