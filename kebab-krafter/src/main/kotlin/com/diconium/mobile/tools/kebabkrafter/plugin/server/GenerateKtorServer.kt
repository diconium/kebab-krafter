package com.diconium.mobile.tools.kebabkrafter.plugin.server

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.ContextSpec
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.generateKtorServerFor
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.artifacts.dsl.FileSystemPublishArtifact
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import java.io.File

object GenerateKtorServer {

    fun apply(target: Project) {

        val input = target.extensions.create("ktorServer", KtorServerExtension::class.java)

        target.afterEvaluate {

            if (input.packageName.isPresent.not() ||
                input.specFile.isPresent.not() ||
                input.contextSpec.packageName.isPresent.not() ||
                input.contextSpec.className.isPresent.not() ||
                input.contextSpec.factoryName.isPresent.not()
            ) {
                return@afterEvaluate
            }

            val sourceFolder: File = if (input.outputFolder.isPresent) {
                input.outputFolder.get()
            } else {
                target.layout.buildDirectory.dir("generated/sources/ktorServer/").get().asFile
            }

            target.setupGenerateTask(sourceFolder, input)

            target.configurations.create("ktorServer") {
                it.isCanBeConsumed = false
                it.isCanBeResolved = true
            }

            target.configurations.create("ktorServerJars") {
                it.isCanBeConsumed = true
                it.isCanBeResolved = false
            }

            val sourceSets = target.extensions.getByType(SourceSetContainer::class.java)


            // TODO: Currently that is the "best" state of this feature until now,
            //  most of the ideas comes from this thread:
            //  https://discuss.gradle.org/t/right-way-to-generate-sources-in-gradle-7/41141/6
            //  But there are some blockers here that I don't know how nor do I have time now to try to solve.
            //  The code here compiles the generated source separated from the main code but 2 dependencies are missing:
            //  - io.ktor.* this should be kinda straight forward,
            //    gotta make sure to allow the application is allowed to overwrite the version
            //  - the context (or CallScope in the sample), that's much trickier as adding a dependency to it
            //    would lead to a circle dependency between the main and the generated code.
            //    I'm not sure this can be fixed without re-thinking re-organizing a bit on how the context is created.

            sourceSets.create("ktorServer") { sourceSet ->
                sourceSet.java.srcDirs(sourceFolder)
                sourceSet.kotlin.srcDirs(sourceFolder)

//                    target.tasks.withType(JavaCompile::class.java).forEach {
//                        println("BUDIUS === >>>  FOUND is $it / ${it::class.java.canonicalName}")
//                        it.dependsOn(GENERATE_CODE_TASK_NAME)
//                        it.classpath = ktorServerSource.output // target.configurations.getByName("ktorServer")
//                    }
            }

            target.tasks.register("compileGeneratedKtorServer", JavaCompile::class.java) { task ->
                task.group = TASK_GROUP
                task.dependsOn(GENERATE_CODE_TASK_NAME)
                task.classpath = target.configurations.getByName("ktorServer")
            }

//            target.tasks.named("assemble"){
//
//            }
//            target.tasks.withType(JavaCompile::class.java).forEach {
//                println("BUDIUS === >>>  FOUND is $it / ${it::class.java.canonicalName}")
//                it.dependsOn(GENERATE_CODE_TASK_NAME)
//                it.classpath = target.configurations.getByName("ktorServer")
//            }
//            target.tasks.whenTaskAdded {
//                println("BUDIUS === >>>  FOUND is $it / ${it::class.java.canonicalName}")
//            }

//            target.tasks.named("assemble", JavaCompile::class.java) {
//                it.dependsOn(GENERATE_CODE_TASK_NAME)
//                it.classpath = target.configurations.getByName("ktorServer")
//            }

            target.tasks.register("ktorServerJar", Jar::class.java) { task ->
                task.group = TASK_GROUP
                task.archiveBaseName.set("ktorServerJar")
                task.from(sourceSets.getByName("ktorServer").output)
                task.dependsOn.add(GENERATE_CODE_TASK_NAME)
            }

            target.artifacts {
                it.add("ktorServerJars", target.tasks.getByName("ktorServerJar"))
            }

            //target.dependencies.add("implementation", target.configurations.getByName("ktorServerJars"))

            //target.dependencies.add("implementation", target.configurations.getByName("ktorServerJars"))

//            target.configurations.named("ktorServerJars") {
//                it.extendsFrom(target.configurations.named("implementation").get())
//            }

            target.configurations.named("implementation") {
                it.extendsFrom(target.configurations.named("ktorServer").get())
            }

            sourceSets.main.configure {

            }


        }
    }
}

private fun Project.setupSourceSets(sourceFolder: File) {
    this.sourceSets { container ->
        container.main.configure { sourceSet ->
            sourceSet.java.srcDirs(sourceFolder)
            sourceSet.kotlin.srcDirs(sourceFolder)
        }
    }
}

private fun Project.setupGenerateTask(sourceFolder: File, input: KtorServerExtension): TaskProvider<Task> {
    return this.tasks.register(GENERATE_CODE_TASK_NAME) { task ->

        task.group = TASK_GROUP
        task.outputs.dir(sourceFolder)

        task.doLast {

            if (input.log) {
                Log.logger = project.logger
            }

            generateKtorServerFor(
                packageName = input.packageName.get(),
                baseDir = sourceFolder,
                specFile = input.specFile.get(),
                contextSpec = ContextSpec(
                    packageName = input.contextSpec.packageName.get(),
                    className = input.contextSpec.className.get(),
                    factoryName = input.contextSpec.factoryName.get(),
                ),
                transformers = input.transformers,
            )
            Log.logger = null
        }
    }
}

// those are copied from those auto-generated accessors files,
// just to make the usage above a bit cleaner.
private fun Project.sourceSets(configure: Action<SourceSetContainer>): Unit =
    (this as ExtensionAware).extensions.configure("sourceSets", configure)

private val SourceSetContainer.main: NamedDomainObjectProvider<SourceSet>
    get() = named("main")

private val SourceSet.kotlin: SourceDirectorySet
    get() = (this as ExtensionAware).extensions.getByName("kotlin")
        as SourceDirectorySet


private const val TASK_GROUP = "kebab-krafter"
private const val GENERATE_CODE_TASK_NAME = "generateKtorServer"
