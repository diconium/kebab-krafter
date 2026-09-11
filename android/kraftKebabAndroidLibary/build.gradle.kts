import com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi

plugins {
	alias(libs.plugins.ktlint)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.parcelize)
	id("io.github.budius.kebab-krafter") version "1.0-SNAPSHOT"
}

android {
	namespace = "io.github.budius.kraftkebabandroidlibary"
	compileSdk {
		version = release(37)
	}

	defaultConfig {
		minSdk = 33

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}

dependencies {
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.core.ktx)
	implementation(libs.material)

	implementation(libs.bundles.ktor.client)
	implementation(libs.kotlinx.serialization)

	testImplementation(libs.junit)
	testImplementation(libs.kotlin.reflect)
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

ktorClient {

	log = true

	create("petStore") {
		packageName = "io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore"
		specFile = File(rootDir, "../sample/src/main/resources/petstore/swagger.yml")
		schemasFolder = File(rootDir, "../sample/src/main/resources/petstore/models")

		// making this false and writting tests to verify they're indeed not parcelable
		// in the app, this is true and the tests verify they're parcelable
		parcelable = false

		@OptIn(KebabKrafterUnstableApi::class)
		transformers {
			endpointTransformer {
				it.copy(
					path = buildList {
						add("base")
						addAll(it.path)
					},
					description = "KtorClient - ${it.description}",
				)
			}
		}
	}
}
