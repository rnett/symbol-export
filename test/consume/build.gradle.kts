plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.rnett.symbol-export.import")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    importSymbols(project(":produce"))
    testImplementation(kotlin("test"))
}