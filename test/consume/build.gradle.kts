plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.rnett.symbol-exporter.import")
}

kotlin {
    jvmToolchain(17)
}

symbolImport {
    autoAddSymbolsDependency = false
}

dependencies {
    importSymbols(project(":produce"))
    implementation("com.rnett.symbol-export:symbols")
    testImplementation(kotlin("test"))
}