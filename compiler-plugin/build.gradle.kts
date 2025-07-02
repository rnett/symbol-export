import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

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

    packageName("com.rnett.symbolexport")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}