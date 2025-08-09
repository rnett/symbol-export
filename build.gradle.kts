plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
}

tasks.register("updateLegacyAbi") {
    dependsOn(project.childProjects.values.map { it.tasks.named("updateLegacyAbi") })
}

tasks.register("checkLegacyAbi") {
    dependsOn(project.childProjects.values.map { it.tasks.named("checkLegacyAbi") })
}

tasks.register("checkAll") {
    group = "verification"
    dependsOn(project.childProjects.values.map { it.tasks.named("check") })
}