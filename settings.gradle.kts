plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

gradle.beforeProject {
    group = "dev.rnett.symbol-export"
    version = "1.0-SNAPSHOT"
}

include(
    ":symbols",
    ":symbols-kotlin-compiler",
    ":symbols-kotlinpoet",
    ":symbols-ksp",
    ":names-internal",
    ":compiler-plugin",
    ":annotations",
    ":gradle-plugins",
    ":generator"
)

includeBuild("gradle/build-logic")

rootProject.name = "symbol-export"