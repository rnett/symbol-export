plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

/*
TODO
 - better tests
 */

tasks.register("updateLegacyAbi") {
    dependsOn(project.childProjects.values.map { it.tasks.named("updateLegacyAbi") })
}

tasks.register("checkLegacyAbi") {
    dependsOn(project.childProjects.values.map { it.tasks.named("checkLegacyAbi") })
}