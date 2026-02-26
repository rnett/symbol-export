package dev.rnett.symbolexport.import

import dev.rnett.symbolexport.BuildConfig
import dev.rnett.symbolexport.Shared
import dev.rnett.symbolexport.export.ExportPlugin
import dev.rnett.symbolexport.kotlinExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.tasks.TaskProvider

/**
 * The import plugin - `dev.rnett.symbol-export.import`.
 *
 * Imports symbols according to [ImportExtension] (`symbolImport`).
 *
 * It does this by registering an incoming configuration `importSymbols` that is compatible with the outgoing configuration added by [ExportPlugin].
 * You add dependencies to the `importSymbols` configuration to specify which symbols to import.
 * The `generateSymbolExports` task will generate symbol definition classes for symbols exported by those dependencies.
 * It is automatically wired as an input for all Kotlin source sets by default - this can be controlled using [ImportExtension.attachToSourceSets].
 *
 * Also automatically adds a dependency on the symbols library (see [ImportExtension.autoAddSymbolsDependency]).
 *
 * @see ImportExtension
 * @see ExportPlugin
 */
public class ImportPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("symbolImport", ImportExtension::class.java).apply {
            generatedSymbolsPackage.convention("symbols")
            autoAddSymbolsDependency.convention(true)
            symbolGenerationDirectory.convention(target.layout.buildDirectory.dir("generated/sources/symbol-export"))
        }

        val helperConfiguration = target.configurations.register("symbolExportRunner") {
            it.isCanBeConsumed = true
        }

        target.dependencies.add(
            helperConfiguration.name,
            "${BuildConfig.KOTLIN_PLUGIN_GROUP}:plugin-support:${BuildConfig.KOTLIN_PLUGIN_VERSION}"
        )

        val importedFiles = extension.importedSymbols.map {
            it.mapValues {
                target.configurations.detachedConfiguration(target.dependencies.create(it.value))
                    .apply {
                        isCanBeResolved = true
                        attributes.attribute(
                            Usage.USAGE_ATTRIBUTE,
                            target.objects.named(Usage::class.java, Shared.USAGE_ATTRIBUTE_VALUE)
                        )
                        incoming.artifactView {
                            it.attributes.attribute(
                                Usage.USAGE_ATTRIBUTE,
                                target.objects.named(Usage::class.java, Shared.USAGE_ATTRIBUTE_VALUE)
                            )
                        }
                    }.resolve().single()
            }
        }

        val generationTask = target.tasks.register("generateSymbols", SymbolGenerationTask::class.java) {
            it.group = "symbol export"
            it.dependencies.set(importedFiles.map {
                it.map { (name, file) ->
                    target.objects.newInstance(SymbolGenerationTask.SymbolDependency::class.java).apply {
                        this.name.set(name)
                        this.file.set(file)
                    }
                }
            })
            it.basePackage.set(extension.generatedSymbolsPackage)
            it.outputDirectory.convention(extension.symbolGenerationDirectory)
            it.classpath.from(helperConfiguration)
        }

        target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            extension.attachToSourceSets.convention(setOf("main"))
            makeKotlinDependOn(target, extension, generationTask)
        }


        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extension.attachToSourceSets.convention(setOf("commonMain"))
            makeKotlinDependOn(target, extension, generationTask)
        }

        target.pluginManager.withPlugin("org.jetbrains.kotlin.android") {
            extension.attachToSourceSets.convention(setOf("main"))
            makeKotlinDependOn(target, extension, generationTask)
        }
    }

    private fun makeKotlinDependOn(
        target: Project,
        extension: ImportExtension,
        task: TaskProvider<SymbolGenerationTask>
    ) {
        val kotlinExtension = target.kotlinExtension
        kotlinExtension.sourceSets.configureEach {
            val sourceSetName = it.name
            val isAttachedToSourceSet = extension.attachToSourceSets.map { sourceSetName in it }

            // this is all a bit hacky - source sets appear to be resolved before the build script is ran, so resolving any properties in this block will cause errors

            it.kotlin.srcDir(
                isAttachedToSourceSet.flatMap {
                    if (it)
                        task.flatMap { it.outputDirectory }
                    else
                        target.layout.buildDirectory.dir("generated/source/non-existent-placeholder/")
                }
            )

            it.dependencies {
                implementation(
                    extension.autoAddSymbolsDependency
                        .flatMap { autoAdd ->
                            extension.attachToSourceSets.apply { finalizeValueOnRead() }
                                .map { autoAdd && sourceSetName in it.orEmpty() }
                        }
                        .map {
                            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                            if (it) BuildConfig.SYMBOLS_LIBRARY_COORDINATES else null
                        }
                )
            }
        }
    }

}