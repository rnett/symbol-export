package build

import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(Shared::toolchain)
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled = true
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}