plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        register("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}

gradle.beforeProject {
    group = "dev.rnett.test"
    version = "1.0-SNAPSHOT"
}

includeBuild("..")

include(":produce", ":consume")

rootProject.name = "test"