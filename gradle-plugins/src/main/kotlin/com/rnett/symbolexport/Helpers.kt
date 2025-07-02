package com.rnett.symbolexport

import org.gradle.api.Project
import org.gradle.api.provider.HasConfigurableValue
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

internal enum class KotlinPluginType {
    Android, Jvm, Multiplatform;
}

internal fun Project.whenKotlinPlugin(block: (KotlinPluginType) -> Unit) {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        block(KotlinPluginType.Jvm)
    }


    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        block(KotlinPluginType.Multiplatform)
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.android") {
        block(KotlinPluginType.Android)
    }
}

internal val Project.kotlinExtension get() = extensions.getByType(KotlinBaseExtension::class.java)

internal fun <T : HasConfigurableValue> T.withDisallowedChanges() = apply {
    disallowChanges()
}