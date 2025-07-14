package dev.rnett.symbolexport

import dev.rnett.symbolexport.internal.ProjectCoordinates
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class PluginComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val outputFile = configuration.getNotNull(CommandLineProcessor.Keys.outputFile)

        val projectName = configuration.getNotNull(CommandLineProcessor.Keys.projectName)

        val projectGroup = configuration.getNotNull(CommandLineProcessor.Keys.projectGroup)
        val projectArtifact = configuration.getNotNull(CommandLineProcessor.Keys.projectArtifact)
        val projectVersion = configuration.getNotNull(CommandLineProcessor.Keys.projectVersion)

        val sourceSetName = configuration.getNotNull(CommandLineProcessor.Keys.sourceSetName)

        val warnOnExported = configuration.get(CommandLineProcessor.Keys.warnOnExported, false)

        FirExtensionRegistrarAdapter.registerExtension(
            PluginRegistrar(
                ExportWriteSpec(
                    outputFile,
                    projectName,
                    ProjectCoordinates(projectGroup, projectArtifact, projectVersion),
                    sourceSetName,
                ),
                warnOnExported
            )
        )
    }
}