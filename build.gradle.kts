import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    id("build.dokka")
}

tasks.register("updateLegacyAbi") {
    dependsOn(project.subprojects.map { it.tasks.named("updateLegacyAbi") })
}

tasks.register("checkLegacyAbi") {
    dependsOn(project.subprojects.map { it.tasks.named("checkLegacyAbi") })
}

tasks.register("checkAll") {
    group = "verification"
    dependsOn(project.subprojects.map { it.tasks.named("check") })
}

tasks.register("assembleAll") {
    group = "build"
    dependsOn(project.subprojects.map { it.tasks.named("assemble") })
}

afterEvaluate {
    tasks.register("publishAllToMavenCentral") {
        group = "publishing"
        dependsOn(project.subprojects.flatMap { it.tasks.named({ it == "publishAllPublicationsToMavenCentralRepository" }) })
    }
    tasks.register("publishAllToMavenLocal") {
        group = "publishing"
        dependsOn(project.subprojects.flatMap { it.tasks.named({ it == "publishToMavenLocal" }) })
    }
}

dependencies {
    dokka(project(":annotations"))
    dokka(project(":gradle-plugins"))
    dokka(project(":symbols"))
    dokka(project(":symbols-kotlin-compiler"))
    dokka(project(":symbols-kotlinpoet"))
    dokka(project(":symbols-ksp"))
}

tasks.withType<KotlinNpmInstallTask>().configureEach {
    if (name == "kotlinWasmNpmInstall")
        mustRunAfter("kotlinNpmInstall")
}

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(24)
}