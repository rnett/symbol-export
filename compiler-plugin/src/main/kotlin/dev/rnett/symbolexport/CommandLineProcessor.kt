package dev.rnett.symbolexport

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.nio.file.Path
import kotlin.io.path.Path

@Suppress("unused") // Used via reflection.
class CommandLineProcessor : CommandLineProcessor {
    companion object {
        val symbolExportFileKey = CompilerConfigurationKey.create<Path>("The file to write exported symbols to")
        val cliOption = CliOption(
            "symbolExportOutputFilePath",
            "<path>",
            "The file to output the exported symbols to",
            required = true,
            allowMultipleOccurrences = false
        )
    }

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = listOf(
        cliOption
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        if (option.optionName == cliOption.optionName) {
            configuration.put(symbolExportFileKey, Path(value))
        } else {
            error("Unexpected config option: '${option.optionName}'")
        }
    }
}