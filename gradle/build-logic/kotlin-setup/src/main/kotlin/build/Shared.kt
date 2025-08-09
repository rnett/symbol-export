package build

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec

internal object Shared {
    fun toolchain(spec: JavaToolchainSpec) = with(spec) {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    fun configureTestTask(task: org.gradle.api.tasks.testing.Test) = with(task) {
        useJUnitPlatform()
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    }

    fun sharedSettings(project: Project, abiValidationEnabled: Provider<Boolean>) = with(project) {
        tasks.named("check") {
            if (abiValidationEnabled.get())
                dependsOn("checkLegacyAbi")
        }
    }
}