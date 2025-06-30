plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.rnett.symbol-exporter.export")
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