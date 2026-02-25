import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("build.kotlin-jvm")
    id("build.publishing")
    alias(libs.plugins.shadow)
}

description = "The symbol export analyzer, powered by the Kotlin analysis API"

dependencies {
    implementation(project(":names-internal"))
    implementation(project(":annotations"))

    listOf(
        libs.kotlin.analysis.api.api,
        libs.kotlin.analysis.api.standalone,
    ).forEach {
        implementation(it) {
            isTransitive = false // see KTIJ-19820
        }
    }
    listOf(
        libs.kotlin.analysis.api.impl,
        libs.kotlin.analysis.api.fir,
        libs.kotlin.low.level.api.fir,
        libs.kotlin.analysis.api.platform,
        libs.kotlin.symbol.light.classes,
    ).forEach {
        runtimeOnly(it) {
            isTransitive = false // see KTIJ-19820
        }
    }
    // copy-pasted from Analysis API https://github.com/JetBrains/kotlin/blob/a10042f9099e20a656dec3ecf1665eea340a3633/analysis/low-level-api-fir/build.gradle.kts#L37
    runtimeOnly("com.github.ben-manes.caffeine:caffeine:2.9.3")

    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlin.compiler.k2)


    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.reflect)

    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    relocate("kotlinx.serialization", "dev.rnett.symbolexport.kotlinx.serialization")
}

tasks.shadowJar {

    archiveClassifier = ""
    mergeServiceFiles()
}

tasks.jar {
    archiveClassifier = "single"
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
    jvmToolchain(8)
}