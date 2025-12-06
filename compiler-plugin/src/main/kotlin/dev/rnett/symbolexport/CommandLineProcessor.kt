package dev.rnett.symbolexport

import dev.rnett.kcp.development.options.BaseCommandLineProcessor
import dev.rnett.`symbol-export`.BuildConfig

@Suppress("unused") // Used via reflection by Kotlin.
class CommandLineProcessor : BaseCommandLineProcessor() {
    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID
    override val options = SymbolExportOptions
}