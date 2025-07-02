package dev.rnett.symbolexport.import

import dev.rnett.symbolexport.BuildConfig
import dev.rnett.symbolexport.Shared
import dev.rnett.symbolexport.generator.SymbolGenerator
import dev.rnett.symbolexport.kotlinExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

public class ImportPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("symbolImport", ImportExtension::class.java).apply {
            generatedSymbolsPackage.convention(target.provider { target.group.toString() })
            autoAddSymbolsDependency.convention(true)
            symbolGenerationDirectory.convention(target.layout.buildDirectory.dir("generated/source/symbol-export"))
            flattenDependencyProjects.convention(false)
        }

        val configuration = target.configurations.register("importSymbols") {
            it.isCanBeConsumed = false
            it.attributes {
                it.attribute(Usage.USAGE_ATTRIBUTE, Shared.USAGE_ATTRIBUTE_VALUE)
            }
        }

        val task = target.tasks.register("generateSymbolExports", ImportSymbolGenerationTask::class.java) {
            it.symbolFiles.from(configuration.get())
            it.outputDirectory.convention(extension.symbolGenerationDirectory)
            it.packageName.convention(extension.generatedSymbolsPackage)
            it.flattenProjects.convention(extension.flattenDependencyProjects)
        }

        target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            extension.attachToSourceSets.convention(setOf("main"))
            makeKotlinDependOn(target, extension, task)
        }


        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extension.attachToSourceSets.convention(setOf("commonMain"))
            makeKotlinDependOn(target, extension, task)
        }

        target.pluginManager.withPlugin("org.jetbrains.kotlin.android") {
            extension.attachToSourceSets.convention(setOf("main"))
            makeKotlinDependOn(target, extension, task)
        }
    }

    public fun makeKotlinDependOn(
        target: Project,
        extension: ImportExtension,
        task: TaskProvider<ImportSymbolGenerationTask>
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

@CacheableTask
public open class ImportSymbolGenerationTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public val symbolFiles: ConfigurableFileCollection = objectFactory.fileCollection()

    @Input
    public val packageName: Property<String> = objectFactory.property(String::class.java)

    @Input
    public val flattenProjects: Property<Boolean> = objectFactory.property(Boolean::class.java)

    @OutputDirectory
    public val outputDirectory: DirectoryProperty = objectFactory.directoryProperty()

    @TaskAction
    public fun doGeneration() {
        val files =
            symbolFiles.files.filter { it.exists() && it.isFile && it.name.endsWith(Shared.SYMBOLS_FILE_EXTENSION) }
        if (files.isEmpty()) {
            didWork = false
            return
        }

        val outputDir = outputDirectory.asFile.get()

        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()

        SymbolGenerator(
            outputDir,
            packageName.get(),
            flattenProjects.get()
        ).writeSymbols(files)
    }

}