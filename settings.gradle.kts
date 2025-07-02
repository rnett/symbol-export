plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

gradle.beforeProject {
    group = "com.rnett.symbol-export"
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
    ":gradle-plugins"
)

includeBuild("gradle/build-logic")

rootProject.name = "symbol-export"