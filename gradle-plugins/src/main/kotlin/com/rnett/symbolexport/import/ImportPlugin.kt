package com.rnett.symbolexport.import

import com.rnett.symbolexport.BuildConfig
import com.rnett.symbolexport.Shared
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
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import javax.inject.Inject

public class ImportPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("symbolImport", ImportExtension::class.java).apply {
            generatedSymbolsPackage.convention(target.provider { target.group.toString() })
            autoAddSymbolsDependency.convention(true)
            symbolGenerationDirectory.convention(target.layout.buildDirectory.dir("generated/source/symbol-export"))
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
        val kotlinExtension = target.extensions.getByType(KotlinBaseExtension::class.java)
        kotlinExtension.sourceSets.configureEach {
            if (it.name in extension.attachToSourceSets.get()) {
                it.kotlin.srcDir(task.flatMap { it.outputDirectory })
                it.dependencies {
                    if (extension.autoAddSymbolsDependency.getOrElse(false)) {
                        implementation(BuildConfig.SYMBOLS_LIBRARY_COORDINATES)
                    }
                }
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

        val outputFile = outputDirectory.asFile.get().resolve(packageName.get().replace('.', '/')).resolve("Symbols.kt")
        outputFile.parentFile.mkdirs()

        if(outputFile.exists()) {
            outputFile.delete()
        }

        outputFile.createNewFile()

        SymbolGenerator.generateSymbols(files, outputFile, packageName.get())
    }

}