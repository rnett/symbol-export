package dev.rnett.symbolexport.import

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

@CacheableTask
public abstract class SymbolGenerationTask @Inject constructor(private val exec: ExecOperations) : DefaultTask() {
    public abstract class SymbolDependency {
        @get:Input
        public abstract val name: Property<String>

        @get:InputFile
        @get:PathSensitive(PathSensitivity.NONE)
        public abstract val file: RegularFileProperty
    }

    @get:Nested
    public abstract val dependencies: ListProperty<SymbolDependency>

    @get:Input
    public abstract val basePackage: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @get:Classpath
    public abstract val classpath: ConfigurableFileCollection

    @TaskAction
    public fun generateSymbols() {
        outputDirectory.get().asFile.deleteRecursively()
        outputDirectory.get().asFile.mkdirs()
        exec.javaexec {
            it.classpath(classpath)
            it.mainClass.set("dev.rnett.symbolexport.generator.GeneratorMain")
            dependencies.finalizeValue()
            it.args = buildList {
                add(outputDirectory.get().asFile.absolutePath)
                add(basePackage.get())
                dependencies.get().forEach {
                    add(it.name.get())
                    add(it.file.get().asFile.absolutePath)
                }
            }
        }
    }
}