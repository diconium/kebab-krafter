import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.plugin)
    alias(libs.plugins.licensee)
    alias(libs.plugins.ktlint)
}

group = "com.diconium.mobile.tools"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.ktor.server)

    implementation(libs.kotlin.poet)
    implementation(libs.kotlin.dateTime)
    implementation(libs.data.json)
    implementation(libs.data.yaml)
    implementation(libs.swagger.parser)

    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        optIn.add("com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi")
    }
}

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allowUrl("https://opensource.org/licenses/MIT")
    allowUrl("http://www.eclipse.org/org/documents/edl-v10.php") { because("Eclipse Distribution License - v 1.0") }
    allowUrl("https://github.com/Him188/yamlkt/blob/master/LICENSE") { because("Apache License 2.0") }
    allowUrl("http://www.mozilla.org/MPL/2.0/index.txt") { because("Mozilla Public License") }
    allowUrl("https://github.com/stleary/JSON-java/blob/master/LICENSE") { because("Public Domain") }
}

ktlint { android = false }

gradlePlugin {
    plugins {
        @Suppress("UnstableApiUsage")
        create("kebabkrafter") {
            id = "com.diconium.mobile.tools.kebab-krafter"
            displayName = "Kebab Krafter"
            implementationClass = "com.diconium.mobile.tools.kebabkrafter.plugin.KebabKrafter"
            description = "Generates all the boring network API code from a Swagger spec."
            website = "https://github.com/diconium/kebab-krafter"
            vcsUrl = "https://github.com/diconium/kebab-krafter.git"
            tags = listOf("swagger", "codegen", "generator", "ktor", "http", "backend")
        }
    }
}
