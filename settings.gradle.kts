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
    ":names-internal",
    ":compiler-plugin",
    ":annotations",
    ":gradle-plugins"
)

rootProject.name = "symbol-export"