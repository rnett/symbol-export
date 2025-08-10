package build

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}