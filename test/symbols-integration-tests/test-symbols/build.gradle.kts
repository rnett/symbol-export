plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.symbol-export.export")
}

kotlin {
    jvmToolchain(17)
}