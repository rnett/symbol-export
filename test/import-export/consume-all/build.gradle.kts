plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.symbol-export.import")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    //TODO update to new dependencies
//    importSymbols(project(":import-export:produce-a"))
//    importSymbols(project(":import-export:produce-b"))
    testImplementation(kotlin("test"))
}