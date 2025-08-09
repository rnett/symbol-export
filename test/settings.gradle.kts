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

include(
    ":import-export:produce-a",
    ":import-export:produce-b",
    ":import-export:consume-a",
    ":import-export:consume-all",
)
include(
    ":symbols-integration-tests:compiler",
    ":symbols-integration-tests:kotlinpoet",
    ":symbols-integration-tests:test-symbols"
)

rootProject.name = "symbol-export-parent"