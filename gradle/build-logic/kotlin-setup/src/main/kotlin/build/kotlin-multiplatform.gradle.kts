package build

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradleExtension

plugins {
    kotlin("multiplatform")
    kotlin("plugin.power-assert")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
configure<PowerAssertGradleExtension> {
    functions.set(listOf("kotlin.test.assertTrue", "kotlin.test.assertEquals", "kotlin.test.assertNotNull", "kotlin.test.assertFalse"))
}

val onlyJvm = providers.systemProperty("symbol-export.onlyJvm").orNull?.lowercase() == "true"

kotlin {
    jvmToolchain(Shared::toolchain)
    jvm()
    js() {
        browser()
        nodejs()
    }
    if (!onlyJvm) {
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

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}