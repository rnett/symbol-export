package build

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

internal object Shared {
    fun toolchain(spec: JavaToolchainSpec) = with(spec) {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    fun configureTestTask(task: org.gradle.api.tasks.testing.Test) = with(task) {
        useJUnitPlatform()
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    }
}

inline fun <reified T : Any> KotlinBaseExtension.extensionIfPresent(block: T.() -> Unit) {
    (this as ExtensionAware)
    this.extensions.findByType(T::class.java)?.apply(block)
}

inline fun <reified T : Any> Project.extensionIfPresent(block: T.() -> Unit) {
    this.extensions.findByType(T::class.java)?.apply(block)
}