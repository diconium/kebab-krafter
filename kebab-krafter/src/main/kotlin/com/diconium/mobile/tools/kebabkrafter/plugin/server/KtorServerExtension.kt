package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File

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
interface KtorServerExtension {

    /**
     * True to enable logging; false otherwise
     */
    @get:Console
    val log: Property<Boolean>

    //region input
    /**
     * Base package name for the generated files.
     */
    @get:Input
    val packageName: Property<String>

    /**
     * Swagger YAML spec file
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    val specFile: Property<File>

    /**
     * Base folder where all the schemas are located (used for Gradle caching)
     */
    @get:InputDirectory
    val schemasFolder: DirectoryProperty

    /**
     * Specification for the custom context where and API call is executed
     */
    @get:Nested
    val contextSpec: ContextSpecExtension

    /**
     * Specification for the custom context where and API call is executed
     */
    fun contextSpec(action: Action<ContextSpecExtension>) {
        action.execute(contextSpec)
    }
    //endregion

    //region output
    /**
     * Output folder for the generated files (defaults to 'build/generated/sources/ktorServer/')
     */
    @get:OutputDirectory
    val outputFolder: DirectoryProperty
    //endregion

    //region transformers
    @get:Nested
    @get:Optional
    @KebabKrafterUnstableApi
    val transformerSpec: TransformerSpec

    /**
     * Specification for the custom transformations for the API
     */
    @KebabKrafterUnstableApi
    fun transformers(action: Action<TransformerSpec>) {
        action.execute(transformerSpec)
    }
//endregion
}


