plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.parcelize)
	alias(libs.plugins.ktlint)

	id("io.github.budius.kebab-krafter") version "1.0-SNAPSHOT"
}

android {
	namespace = "io.github.budius.kebabkrafterandroid"
	compileSdk {
		version = release(37)
	}

	defaultConfig {
		applicationId = "io.github.budius.kebabkrafterandroid"
		minSdk = 33
		targetSdk = 37
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			optimization {
				enable = false
			}
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}

ktlint {
	android = true
	filter {
		// https://github.com/JLLeitschuh/ktlint-gradle/issues/751
		exclude { element ->
			val path = element.file.path
			path.contains("\\generated\\") || path.contains("/generated/")
		}
	}
}

dependencies {
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.core.ktx)
	implementation(libs.material)

	implementation(libs.bundles.ktor.client)
	implementation(libs.kotlinx.serialization)
	implementation(project(":kraftKebabAndroidLibary"))

	testImplementation(libs.junit)
	testImplementation(libs.kotlin.reflect)
}

ktorClient {

	log = true

	create("petStore") {
		packageName = "io.github.budius.kebabkrafterandroid.gen.android.petstore"
		specFile = File(rootDir, "../sample/src/main/resources/petstore/swagger.yml")
		schemasFolder = File(rootDir, "../sample/src/main/resources/petstore/models")
	}
}
