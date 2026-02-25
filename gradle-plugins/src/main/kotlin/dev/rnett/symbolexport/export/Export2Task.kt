package dev.rnett.symbolexport.export

import dev.rnett.symbolexport.internal.AnalysisArguments
import dev.rnett.symbolexport.internal.ProjectCoordinates
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

//TODO make incremental
@CacheableTask
internal abstract class Export2Task @Inject constructor(private val exec: ExecOperations) : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val analysisClasspath: ConfigurableFileCollection

    @get:Classpath
    abstract val runnerClasspath: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val projectGroup: Property<String>

    @get:Input
    abstract val projectVersion: Property<String>

    @get:Input
    abstract val sourceSetName: Property<String>

    @get:Input
    abstract val targetName: Property<String>

    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val jdkHome: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @TaskAction
    fun execute() {
        val args = AnalysisArguments(
            projectName.get(),
            ProjectCoordinates(
                projectGroup.get(),
                projectName.get(),
                projectVersion.get(),
            ),
            sourceSetName.get(),
            targetName.get(),
            jdkHome.orNull?.asFile?.absolutePath,
            sources.files.map { it.absolutePath },
            analysisClasspath.files.map { it.absolutePath },
            outputDir.get().asFile.absolutePath
        )
        val argsFile = temporaryDir.resolve("args.json")
        argsFile.writeText(args.serialize())

        exec.javaexec {
            it.classpath = this.runnerClasspath
            it.mainClass.set("dev.rnett.symbolexport.analyzer.SymbolAnalyzer")
            it.args(argsFile.absolutePath)
        }

    }
}