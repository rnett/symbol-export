import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("build.kotlin-jvm")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))

    implementation(libs.kotlinx.serialization.json)
    implementation(project(":names-internal"))
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
    }
    relocate("kotlinx.serialization", "dev.rnett.symbolexport.kotlinx.serialization")
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