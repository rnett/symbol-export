package dev.rnett.symbolexport

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.nio.file.Path
import kotlin.io.path.Path
import dev.rnett.`symbol-export`.BuildConfig

@Suppress("unused") // Used via reflection.
class CommandLineProcessor : CommandLineProcessor {


    object Keys {
        val outputFile = CompilerConfigurationKey.create<Path>("The file to write exported symbols to")

        val projectName = CompilerConfigurationKey.create<String>("The configured name of the gradle project")

        val projectGroup = CompilerConfigurationKey.create<String>("The group of the gradle project")
        val projectArtifact = CompilerConfigurationKey.create<String>("The artifact of the gradle project")
        val projectVersion = CompilerConfigurationKey.create<String>("The version of the gradle project")
        val sourceSetName = CompilerConfigurationKey.create<String>("The source set name")
        val warnOnExported = CompilerConfigurationKey.create<Boolean>("Whether to add a warning diagnostic on exported symbols")
    }

    object Options {
        val outputFile = CliOption(
            "outputFile",
            "<path>",
            "The file to output the exported symbols to",
            required = true,
            allowMultipleOccurrences = false
        )

        val projectName = CliOption(
            "projectName",
            "<string>",
            "The configured name of the gradle project",
            required = true,
            allowMultipleOccurrences = false
        )

        val projectGroup = CliOption(
            "projectGroup",
            "<string>",
            "The group of the gradle project",
            required = true,
            allowMultipleOccurrences = false
        )
        val projectArtifact = CliOption(
            "projectArtifact",
            "<string>",
            "The artifact of the gradle project",
            required = true,
            allowMultipleOccurrences = false
        )
        val projectVersion = CliOption(
            "projectVersion",
            "<string>",
            "The gradle project version",
            required = true,
            allowMultipleOccurrences = false
        )
        val sourceSetName = CliOption(
            "sourceSetName",
            "<string>",
            "The source set name",
        )
        val warnOnExported = CliOption(
            "warnOnExported",
            "<true|false>",
            "Whether to add a warning diagnostic on exported symbols",
            required = false
        )
    }

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = listOf(
        Options.outputFile,
        Options.projectName,
        Options.projectGroup,
        Options.projectArtifact,
        Options.projectVersion,
        Options.sourceSetName,
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option) {
            Options.outputFile -> configuration.put(Keys.outputFile, Path(value))
            Options.projectName -> configuration.put(Keys.projectName, value)
            Options.projectGroup -> configuration.put(Keys.projectGroup, value)
            Options.projectArtifact -> configuration.put(Keys.projectArtifact, value)
            Options.projectVersion -> configuration.put(Keys.projectVersion, value)
            Options.sourceSetName -> configuration.put(Keys.sourceSetName, value)
            Options.warnOnExported -> configuration.put(Keys.warnOnExported, value.toBoolean())
            else -> error("Unexpected config option: '${option.optionName}'")
        }
    }
}