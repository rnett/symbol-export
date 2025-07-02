plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("dev.rnett.symbol-export.export")
}

kotlin {
    jvmToolchain(17)

    jvm()
    js() {
        browser()
    }

    val commonMain by sourceSets.getting {
    }
}