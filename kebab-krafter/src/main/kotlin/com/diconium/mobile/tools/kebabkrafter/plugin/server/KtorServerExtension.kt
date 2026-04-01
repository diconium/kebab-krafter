package com.diconium.mobile.tools.kebabkrafter.plugin.server

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

/**
 * Configuration for the 'generateKtorServer' task.
 *
 * Example configuration:
 * ```
 * ktorServer.default {
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
 *
 * In the example above:
 * - The code will be generated in 'src/main/kotlin/a/b/c/'
 * - The generated code will invoke the function `a.b.c.CallScope.from(ApplicationCall): CallScope` to get new instance
 *   of the `CallScope`
 * - The generated interfaces functions will be `suspend fun CallScope.execute(params)`
 *
 * Alternatively to generate more than one server interface, use `create` function:
 * ```
 * ktorServer {
 *
 *   log = true (defaults to false)
 *
 *   create("main") {
 *      // this creates the task `generateMainKtorServer`
 *      ... configuration for the main server
 *   }
 *
 *   create("health") {
 *      // this creates the task `generateHealthKtorServer`
 *      ... configuration for the health endpoints
 *   }
 * }
 * ```
 *
 */
interface KtorServerExtension {

    /**
     * True to enable logging; false otherwise
     * defaults to: false
     */
    @get:Console
    val log: Property<Boolean>

    @get:Nested
    @get:Input
    val services: NamedDomainObjectContainer<KtorServerServiceExtension>

    /**
     * Invoke this function to create and configure a KtorService
     */
    fun create(name: String, block: Action<KtorServerServiceExtension>) {
        services.create(name, block)
    }
}
