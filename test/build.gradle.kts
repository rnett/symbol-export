import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.withType<KotlinNpmInstallTask>().configureEach {
    if (name == "kotlinWasmNpmInstall")
        mustRunAfter("kotlinNpmInstall")
}

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(24)
}