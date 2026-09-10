import com.diconium.mobile.tools.kebabkrafter.KebabKrafterUnstableApi

plugins {
	alias(libs.plugins.android.library)
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
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(libs.androidx.junit)

	implementation(libs.bundles.ktor.client)
	implementation(libs.kotlinx.serialization)
}

ktorClient {

	log = true

	create("petStore") {
		packageName = "io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore"
		specFile = File(rootDir, "../sample/src/main/resources/petstore/swagger.yml")
		schemasFolder = File(rootDir, "../sample/src/main/resources/petstore/models")

		@OptIn(KebabKrafterUnstableApi::class)
		transformers {
			endpointTransformer {
				it.copy(
					path = buildList {
						add("base")
						addAll(it.path)
					},
					description = "KtorClient - ${it.description}"
				)
			}
		}
	}
}

