package build

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

val onlyJvm = providers.systemProperty("symbol-export.onlyJvm").orNull?.lowercase() == "true"

kotlin {
    jvmToolchain(Shared::toolchain)
    jvm()
    if (!onlyJvm) {
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

        // Apple
        macosX64()
        macosArm64()
        iosArm64()
        iosX64()
        iosSimulatorArm64()
        watchosArm32()
        watchosArm64()
        watchosX64()
        watchosSimulatorArm64()
        watchosDeviceArm64()
        tvosArm64()
        tvosX64()
        tvosSimulatorArm64()

        // Linux
        linuxX64()
        linuxArm64()

        // Windows
        mingwX64()

        // Android Native
        androidNativeArm32()
        androidNativeArm64()
        androidNativeX86()
        androidNativeX64()
    }

    applyDefaultHierarchyTemplate()
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}