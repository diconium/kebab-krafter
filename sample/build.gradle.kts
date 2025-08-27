import com.diconium.mobile.tools.kebabkrafter.generator.DefaultKtorControllerMapper
import com.diconium.mobile.tools.kebabkrafter.generator.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.KtorMapper
import com.diconium.mobile.tools.kebabkrafter.generator.KtorTransformer
import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint
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

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.datetime)
    implementation(libs.koin)
    implementation(libs.logback)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
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

    val version = endpoint.path
        .firstOrNull()
        .takeIf { it?.matches("v[0-9]+".toRegex()) == true }
        ?.substring(1)
        ?.toInt()

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
val ktorMapper = KtorMapper { shortestPath: Int, dataSpecs: Map<String, BaseSpecModel>, endpoint: Endpoint ->
    val ctrl: KtorController = DefaultKtorControllerMapper.map(shortestPath, dataSpecs, endpoint)

    val version = endpoint.path
        .first()
        .takeIf { it.startsWith("v") && it.trimStart('v').toIntOrNull() != null }

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
    packageName = "com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore"
    specFile = File(rootDir, "src/main/resources/petstore/swagger.yml")

    // use this for local testing your own APIs
    // specFile = File(rootDir, "test-data/api.yml")

    contextSpec {
        packageName = "com.diconium.mobile.tools.kebabkrafter.sample"
        className = "CallScope"
        factoryName = "from"
    }

    // The transformers allow to manipulate the parsed data before code generation
    // with great power comes great responsibility, use it with care
    // 	transformers {
    // 		ktorMapper(ktorMapper)
    // 	}
}
