package build

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradleExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.power-assert")
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
configure<PowerAssertGradleExtension> {
    functions.set(listOf("kotlin.test.assertTrue", "kotlin.test.assertEquals", "kotlin.test.assertNotNull", "kotlin.test.assertFalse"))
}

kotlin {
    jvmToolchain(Shared::toolchain)

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    Shared.configureTestTask(this)
}