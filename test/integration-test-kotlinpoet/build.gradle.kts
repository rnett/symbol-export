plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.symbol-export.import")
}


dependencies {
    importSymbols(project(":test-symbols"))
    testImplementation(project(":test-symbols"))

    testImplementation("dev.rnett.symbol-export:symbols-kotlinpoet")

    testImplementation(kotlin("test-junit5"))
}