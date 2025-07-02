import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("build.kotlin-jvm")
    alias(libs.plugins.buildconfig)
}



dependencies {
    compileOnly(kotlin("compiler"))

    implementation(libs.kotlinx.serialization.json)
    implementation(project(":names-internal"))
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }

    packageName("dev.rnett.symbolexport")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")
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