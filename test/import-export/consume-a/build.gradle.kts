plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.symbol-export.import")
}

kotlin {
    jvmToolchain(17)
}

symbolImport {
    import("a", project(":import-export:produce-a"))
}

dependencies {
    testImplementation(kotlin("test"))
}