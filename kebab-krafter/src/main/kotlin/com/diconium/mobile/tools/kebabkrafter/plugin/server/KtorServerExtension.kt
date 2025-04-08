package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.generator.Transformers
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

/**
 * Configuration for the 'generateKtorInterface' task.
 *
 * Example configuration:
 * ```
 * ktorServer {
 * 	packageName = "a.b.c"
 * 	baseDir = File(projectDir, "src/main/kotlin/")
 * 	specFile = File(rootDir, "src/main/resources/petstore/swagger.yml")
 * 	contextSpec {
 * 		packageName = "a.b.c"
 * 		className = "CallScope"
 * 		factoryName = "from"
 * 	}
 * }
 * ```
 * In the example above:
 * - The code will be generated in 'src/main/kotlin/a/b/c/'
 * - The generated code will invoke the function `a.b.c.CallScope.from(ApplicationCall): CallScope` to get new instance
 *   of the `CallScope`
 * - The generated interfaces functions will be `suspend fun CallScope.execute(params)`
 */
open class KtorServerExtension @Inject constructor(objects: ObjectFactory) {

	/**
	 * True to enable logging
	 */
	var log: Boolean = false

	/**
	 * Base package name for the generated files.
	 */
	val packageName: Property<String> = objects.property(String::class.java)

	/**
	 * Swagger YAML spec file
	 */
	val specFile: Property<File> = objects.property(File::class.java)

	/**
	 * Specification for the custom context where and API call is executed
	 */
	internal val contextSpec: ContextSpecExtension = objects.newInstance(ContextSpecExtension::class.java)

	/**
	 * Specification for the custom context where and API call is executed
	 */
	fun contextSpec(action: Action<ContextSpecExtension>) {
		action.execute(contextSpec)
	}

	internal val transformers = Transformers()
	/**
	 * Specification for the custom transformations for the API
	 */
	fun transformers(action: Action<Transformers>) {
		action.execute(transformers)
	}
}
