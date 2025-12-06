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
    linuxX64()
    mingwX64()

    val commonMain by sourceSets.getting {
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}