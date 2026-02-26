package dev.rnett.symbolexport.export

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

@CacheableTask
public abstract class PostProcessSymbolsTask
@Inject constructor(
    private val exec: ExecOperations
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val symbols: DirectoryProperty

    @get:Input
    public abstract val projectSymbolName: Property<String>

    @get:Classpath
    public abstract val classpath: ConfigurableFileCollection

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @TaskAction
    public fun processSymbols() {
        exec.javaexec {
            it.classpath(classpath)
            it.mainClass.set("dev.rnett.symbolexport.postprocessor.PostProcessorMain")
            it.args(
                symbols.get().asFile.absolutePath,
                outputFile.get().asFile.absolutePath,
                projectSymbolName.get()
            )
        }
    }
}