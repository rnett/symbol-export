plugins {
    id("build.kotlin-multiplatform")
    id("build.public-module")
}

description = "The symbol types used to generate the symbols model when importing symbols"

dependencies {
    commonMainImplementation(libs.kotlinx.immutable.collections)
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("dev.rnett.symbolexport.symbol.SymbolExportInternals")
    }
}