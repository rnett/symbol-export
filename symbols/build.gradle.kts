plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(17)
    jvm()
    js() {
        browser()
        nodejs()
    }
    wasmJs() {
        d8()
        nodejs()
        browser()
    }
    wasmWasi() {
        nodejs()
    }
    iosArm64()
    iosX64()
    mingwX64()
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()

    val commonMain by sourceSets.getting {
        dependencies {
        }
    }
    sourceSets.configureEach {
        explicitApi()
        languageSettings {
        }
    }
}