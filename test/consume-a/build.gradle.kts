plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.symbol-export.import")
}

kotlin {
    jvmToolchain(17)
}

symbolImport {
    flattenDependencyProjects = true
}

dependencies {
    importSymbols(project(":produce-a"))
    testImplementation(kotlin("test"))
}