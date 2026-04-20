package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class KtorServerServiceExtension(@get:Input val name: String) {

    //region input
    /**
     * Base package name for the generated files.
     */
    @get:Input
    abstract val packageName: Property<String>

    /**
     * Swagger YAML spec file
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val specFile: Property<File>

    /**
     * Base folder where all the schemas are located (used for Gradle caching)
     */
    @get:InputDirectory
    abstract val schemasFolder: DirectoryProperty

    /**
     * Specification for the custom context where and API call is executed
     */
    @get:Nested
    abstract val contextSpec: ContextSpecExtension

    /**
     * Name of the route installation function
     */
    @get:Input
    abstract val installFunction: Property<String>

    /**
     * Specification for the custom context where and API call is executed
     */
    fun contextSpec(action: Action<ContextSpecExtension>) {
        action.execute(contextSpec)
    }
    //endregion

    //region output
    /**
     * Output folder for the generated files
     * defaults to: build/generated/sources/ktorServer/
     */
    @get:OutputDirectory
    @get:Optional
    abstract val outputFolder: DirectoryProperty
    //endregion

    //region transformers
    @get:Nested
    @get:Optional
    @KebabKrafterUnstableApi
    abstract val transformerSpec: TransformerSpec

    /**
     * Specification for the custom transformations for the API
     */
    @KebabKrafterUnstableApi
    fun transformers(action: Action<TransformerSpec>) {
        action.execute(transformerSpec)
    }
//endregion
}
