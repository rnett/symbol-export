package dev.rnett.symbolexport.export

import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * Do not export symbols from this source set.
 *
 * @see ExportExtension
 */
public fun KotlinSourceSet.ignoreSymbols() {
    this.project.extensions.getByType(ExportExtension::class.java).ignoreSourceSets.add(this.name)
}