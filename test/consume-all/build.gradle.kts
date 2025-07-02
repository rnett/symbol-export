plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.symbol-export.import")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    importSymbols(project(":produce-a"))
    importSymbols(project(":produce-b"))
    testImplementation(kotlin("test"))
}