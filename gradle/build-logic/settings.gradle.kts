plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        register("libs") { from(files("../libs.versions.toml")) }
    }
}

rootProject.name = "build-logic"
include("kotlin-setup")