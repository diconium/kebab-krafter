//rootProject.name = "Kebab Krafter Generator"
rootProject.name = "kebab-krafter"

plugins {

	// See https://jmfayard.github.io/refreshVersions
	id("de.fayard.refreshVersions") version "0.60.3"
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

refreshVersions {
	rejectVersionIf {
		candidate.stabilityLevel != de.fayard.refreshVersions.core.StabilityLevel.Stable
	}
}
