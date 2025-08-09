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

dependencies {
    dokka(project(":annotations"))
    dokka(project(":gradle-plugins"))
    dokka(project(":symbols"))
    dokka(project(":symbols-kotlin-compiler"))
    dokka(project(":symbols-kotlinpoet"))
    dokka(project(":symbols-ksp"))
}