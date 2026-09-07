import com.diconium.mobile.tools.kebabkrafter.*
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import com.diconium.mobile.tools.kebabkrafter.models.JsonSpecFile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    id("io.github.budius.kebab-krafter") version "1.0-SNAPSHOT"

    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.licensee)
    alias(libs.plugins.ktlint)
}

group = "com.diconium.mobile.tools.networkgenerator.sample"
version = "0.0.1"

application {
    mainClass.set("MainKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

licensee {
    allow("Apache-2.0")
    allow("EPL-2.0")
    allowUrl("https://opensource.org/license/mit")
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.koin)
    implementation(libs.logback)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

ktlint {
    android = false
    filter {
        // https://github.com/JLLeitschuh/ktlint-gradle/issues/751
        exclude { element ->
            val path = element.file.path
            path.contains("\\generated\\") || path.contains("/generated/")
        }
    }
}

// here are examples of crazy manipulations possible with the KtorTransformer
// but those are not being applied to the sample app
private val ktorTransformer = KtorTransformer { endpoint, ctrl ->
    // this is not used in the sample app, but it's here mostly as an example

    val version = endpoint.path.firstOrNull().takeIf { it?.matches("v[0-9]+".toRegex()) == true }?.substring(1)?.toInt()

    if (version != null) {
        println("transforming: ${ctrl.ktorFunction} ${ctrl.path.joinToString("/")}")
        ctrl.copy(
            path = ctrl.path.let { it.subList(1, it.size) },
            routeHeaders = listOf("X-Api-Version" to "v$version"),
        )
    } else {
        ctrl
    }
}

// here are examples of crazy manipulations possible with the KtorMapper
// but those are not being applied to the sample app
val customKtorMapper = KtorMapper { shortestPath: Int, endpoint: Endpoint, dataSpecs: Map<String, JsonSpecFile> ->
    val ctrl: KtorController = DefaultKtorControllerMapper.map(shortestPath, endpoint, dataSpecs)

    val version = endpoint.path.first().takeIf { it.startsWith("v") && it.trimStart('v').toIntOrNull() != null }

    if (version != null) {
        ctrl.copy(

            // remove the version from the route
            path = ctrl.path.let { it.subList(1, it.size) },

            // add version to header
            routeHeaders = listOf("X-Api-Version" to version.trimStart('v')),

            packageName = "controllers.${endpoint.path[1]}.$version".replace("-", "_"),
        )
    } else {
        ctrl.copy(
            packageName = "controllers.other.${ctrl.packageName.replace("controllers", "")}",
        )
    }
}

ktorServer {
    log = true

    // The PetStore example
    create("petStore") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore"
        specFile = File(rootDir, "src/main/resources/petstore/swagger.yml")
        schemasFolder = File(rootDir, "src/main/resources/petstore/models")

        // use this for local testing your own APIs
        // specFile = File(rootDir, "test-data/api.yml")

        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }

    //region development/testing of edge cases and complex data structures
    create("caseMaps") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.maps"
        specFile = File(rootDir, "testCases/maps/swagger.yml")
        schemasFolder = File(rootDir, "testCases/maps/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("caseAcronym") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.acronym"
        specFile = File(rootDir, "testCases/acronym/swagger.yml")
        schemasFolder = File(rootDir, "testCases/acronym/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("caseInlined") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlined"
        specFile = File(rootDir, "testCases/inlined/swagger.yml")
        schemasFolder = File(rootDir, "testCases/inlined/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("caseDescriptions") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.descriptions"
        specFile = File(rootDir, "testCases/descriptions/swagger.yml")
        schemasFolder = File(rootDir, "testCases/descriptions/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("security") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.security"
        specFile = File(rootDir, "testCases/security/swagger.yml")
        schemasFolder = File(rootDir, "testCases/security/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample.cases.security"
            className = "SecureCallScope"
            factoryName = "from"
        }
    }
    create("caseInlineInSealedClass") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass"
        specFile = File(rootDir, "testCases/caseInlineInSealedClass/swagger.yml")
        schemasFolder = File(rootDir, "testCases/caseInlineInSealedClass/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("headersRoute") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.headersRoute"
        specFile = File(rootDir, "testCases/headersRoute/swagger.yml")
        schemasFolder = File(rootDir, "testCases/headersRoute/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }

        // The transformers allow to manipulate the parsed data before code generation
        // with great power comes great responsibility, use it with care
        @OptIn(KebabKrafterUnstableApi::class)
        transformers {

            ktorTransformer { endpoint, ctrl ->

                // find if this endpoint starts with a version number
                val version = endpoint.path
                    .firstOrNull()
                    .takeIf { it?.matches("v[0-9]+".toRegex()) == true }
                    ?.substring(1)
                    ?.toInt()

                if (version != null) {
                    // move the version information to the header
                    ctrl.copy(

                        // remove the version from the route
                        path = endpoint.path.drop(1),

                        // add version to header
                        routeHeaders = listOf("X-Api-Version" to version.toString()),

                        kdoc = ctrl.kdoc
                            ?.split("\n")
                            ?.joinToString(separator = "\n") {
                                val remove = "v$version/"
                                if (it.contains(remove)) {
                                    "X-Api-Version: $version\n${it.replace(remove, "")}"
                                } else {
                                    it
                                }
                            },
                    )
                } else {
                    // redirect non-api controllers (a.k.a /cloud)
                    ctrl.copy(
                        packageName = ctrl.packageName.replace("controllers", "controllers.cloud"),
                    )
                }
            }
        }
    }
    //endregion
}

ktorClient {
    // The PetStore example
    create("petStore") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore"
        specFile = File(rootDir, "src/main/resources/petstore/swagger.yml")
        schemasFolder = File(rootDir, "src/main/resources/petstore/models")
    }

    //region development/testing of edge cases and complex data structures
    create("caseMaps") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.maps"
        specFile = File(rootDir, "testCases/maps/swagger.yml")
        schemasFolder = File(rootDir, "testCases/maps/models/")
    }
    create("caseAcronym") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.acronym"
        specFile = File(rootDir, "testCases/acronym/swagger.yml")
        schemasFolder = File(rootDir, "testCases/acronym/models/")
    }
    create("caseInlined") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.inlined"
        specFile = File(rootDir, "testCases/inlined/swagger.yml")
        schemasFolder = File(rootDir, "testCases/inlined/models/")
    }
    create("caseDescriptions") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.descriptions"
        specFile = File(rootDir, "testCases/descriptions/swagger.yml")
        schemasFolder = File(rootDir, "testCases/descriptions/models/")
    }
    create("security") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.security"
        specFile = File(rootDir, "testCases/security/swagger.yml")
        schemasFolder = File(rootDir, "testCases/security/models/")
    }
    create("caseInlineInSealedClass") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.inlinesealedclass"
        specFile = File(rootDir, "testCases/caseInlineInSealedClass/swagger.yml")
        schemasFolder = File(rootDir, "testCases/caseInlineInSealedClass/models/")
    }
    create("headersRoute") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.headersRoute"
        specFile = File(rootDir, "testCases/headersRoute/swagger.yml")
        schemasFolder = File(rootDir, "testCases/headersRoute/models/")

        // The transformers allow to manipulate the parsed data before code generation
        // with great power comes great responsibility, use it with care
        @OptIn(KebabKrafterUnstableApi::class)
        transformers {
            ktorTransformer { endpoint, ctrl ->

                // find if this endpoint starts with a version number
                val version = endpoint.path
                    .firstOrNull()
                    .takeIf { it?.matches("v[0-9]+".toRegex()) == true }
                    ?.substring(1)
                    ?.toInt()

                if (version != null) {
                    // move the version information to the header
                    ctrl.copy(

                        // remove the version from the route
                        path = endpoint.path.drop(1),

                        // add version to header
                        routeHeaders = listOf("X-Api-Version" to version.toString()),

                        kdoc = ctrl.kdoc
                            ?.split("\n")
                            ?.joinToString(separator = "\n") {
                                val remove = "v$version/"
                                if (it.contains(remove)) {
                                    "X-Api-Version: $version\n${it.replace(remove, "")}"
                                } else {
                                    it
                                }
                            },
                    )
                } else {
                    // redirect non-api controllers (a.k.a /cloud)
                    ctrl.copy(
                        packageName = ctrl.packageName.replace("controllers", "controllers.cloud"),
                    )
                }
            }
        }
    }
    //endregion
}
