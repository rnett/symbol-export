package dev.rnett.symbolexport

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class PluginComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val outputFile = configuration.getNotNull(CommandLineProcessor.symbolExportFileKey)
        FirExtensionRegistrarAdapter.registerExtension(PluginRegistrar(outputFile))
    }
}