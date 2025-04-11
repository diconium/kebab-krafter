import com.diconium.mobile.tools.kebabkrafter.generator.DefaultKtorControllerMapper
import com.diconium.mobile.tools.kebabkrafter.generator.KtorController
import com.diconium.mobile.tools.kebabkrafter.generator.KtorMapper
import com.diconium.mobile.tools.kebabkrafter.generator.KtorTransformer
import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.Endpoint

private val ktorVersion: String = "2.3.12"
private val kotlinVersion: String = "1.9.24"
private val logbackVersion: String = "1.4.14"

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.22"
    id("io.ktor.plugin") version "2.3.10"
    id("com.diconium.mobile.tools.kebab-krafter") version "1.0-SNAPSHOT"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
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
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cio-jvm")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    implementation("io.insert-koin:koin-core:3.5.6")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
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
