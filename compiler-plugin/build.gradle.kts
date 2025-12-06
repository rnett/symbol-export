import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("build.kotlin-jvm")
    id("build.publishing")
    // KCP-Development: unified plugin for Kotlin compiler plugin projects
    alias(libs.plugins.kcp.dev.compiler)
}

description = "The compiler plugin that powers symbol-export by exporting marked symbols as JSON"

// Configure the KCP-Development extension with our registrar and CLI processor
compilerPluginDevelopment {
    compilerPluginRegistrar = "dev.rnett.symbolexport.PluginComponentRegistrar"
    commandLineProcessor = "dev.rnett.symbolexport.CommandLineProcessor"
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))

    implementation(project(":names-internal"))

    // Add our runtime inputs for compiler tests (configuration provided by kcp plugin)
    add("compilerTestRuntimeClasspath", project(":annotations"))
}

tasks.shadowJar {
    relocate("kotlinx.serialization", "dev.rnett.symbolexport.kotlinx.serialization")
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    explicitApi = ExplicitApiMode.Disabled
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation { enabled = false }
}