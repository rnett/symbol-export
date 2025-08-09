package build

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(Shared::toolchain)
    jvm()
    js() {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs() {
        d8()
        nodejs()
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
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

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
        klib {
            enabled = true
            keepUnsupportedTargets = true
        }
    }
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}

@OptIn(ExperimentalAbiValidation::class)
Shared.sharedSettings(project, kotlin.abiValidation.enabled)