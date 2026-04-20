import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.DefaultKtorControllerMapper
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.KtorMapper
import com.diconium.mobile.tools.kebabkrafter.generator.ktorserver.KtorTransformer
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
import com.diconium.mobile.tools.kebabkrafter.models.JsonSpecFile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {

    id("com.diconium.mobile.tools.kebab-krafter") version "1.0-SNAPSHOT"

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
    allow("EPL-1.0")
    allowUrl("https://opensource.org/license/mit")
}

dependencies {
    implementation(libs.bundles.ktor)
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
        println("transforming: ${ctrl.ktorFunction} ${ctrl.route}")
        ctrl.copy(
            route = ctrl.route.split("/").let { it.subList(1, it.size) }.joinToString("/"),
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
            route = ctrl.route.split("/").let { it.subList(1, it.size) }.joinToString("/"),

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
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore"
        specFile = File(rootDir, "src/main/resources/petstore/swagger.yml")
        schemasFolder = File(rootDir, "src/main/resources/petstore/models")

        // use this for local testing your own APIs
        // specFile = File(rootDir, "test-data/api.yml")

        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }

        // The transformers allow to manipulate the parsed data before code generation
        // with great power comes great responsibility, use it with care

//        @OptIn(KebabKrafterUnstableApi::class)
//        transformers {
//            ktorMapper(customKtorMapper)
//        }
    }

    //region development/testing of edge cases and complex data structures
    create("caseMaps") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps"
        specFile = File(rootDir, "testCases/maps/swagger.yml")
        schemasFolder = File(rootDir, "testCases/maps/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("caseAcronym") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.case.acronym"
        specFile = File(rootDir, "testCases/acronym/swagger.yml")
        schemasFolder = File(rootDir, "testCases/acronym/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("caseInlined") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined"
        specFile = File(rootDir, "testCases/inlined/swagger.yml")
        schemasFolder = File(rootDir, "testCases/inlined/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    create("caseDescriptions") {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.case.descriptions"
        specFile = File(rootDir, "testCases/descriptions/swagger.yml")
        schemasFolder = File(rootDir, "testCases/descriptions/models/")
        contextSpec {
            packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
            className = "CallScope"
            factoryName = "from"
        }
    }
    //endregion
}
