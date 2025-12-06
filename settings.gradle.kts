pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

val versionFile = providers.fileContents(layout.rootDirectory.file("version.txt"))

gradle.beforeProject {
    group = "dev.rnett.symbol-export"
    version = versionFile.asText.get().trim()
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