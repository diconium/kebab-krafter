package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.generator.Transformers
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
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
abstract class KtorServerExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * True to enable logging; false otherwise
     */
    @get:Console
    val log: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /**
     * Base package name for the generated files.
     */
    @get:Input
    val packageName: Property<String> = objects.property(String::class.java)

    /**
     * Swagger YAML spec file
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    val specFile: Property<File> = objects.property(File::class.java)

    @get:InputDirectory
    abstract val schemasFolder: DirectoryProperty

    /**
     * Output folder for the generated files (defaults to 'build/generated/sources/ktorServer/').
     * Note: only the default folder gets automatically added to the target sourceSet.
     */
    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    /**
     * Specification for the custom context where and API call is executed
     */
    @get:Nested
    internal val contextSpec: ContextSpecExtension = objects.newInstance(ContextSpecExtension::class.java)

    /**
     * Specification for the custom context where and API call is executed
     */
    fun contextSpec(action: Action<ContextSpecExtension>) {
        action.execute(contextSpec)
    }

    // TODO: this should "somehow" also be an input
    @get:Internal
    internal val transformers = Transformers()

    /**
     * Specification for the custom transformations for the API
     */
    fun transformers(action: Action<Transformers>) {
        action.execute(transformers)
    }
}
