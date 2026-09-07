package com.diconium.mobile.tools.kebabkrafter.plugin.client

import com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi
import com.diconium.mobile.tools.kebabkrafter.plugin.TransformerSpec
import org.gradle.api.Action
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class KtorClientServiceExtension(@get:Input val name: String) {

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
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val specFile: Property<File>

    /**
     * Base folder where all the schemas are located (used for Gradle caching)
     */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val schemasFolder: DirectoryProperty
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
