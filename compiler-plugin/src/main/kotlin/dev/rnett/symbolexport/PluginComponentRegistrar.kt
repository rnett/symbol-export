package dev.rnett.symbolexport

import dev.rnett.kcp.development.options.get
import dev.rnett.kcp.development.registrar.BaseSpecCompilerPluginRegistrar
import dev.rnett.`symbol-export`.BuildConfig
import dev.rnett.symbolexport.internal.ProjectCoordinates
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class PluginComponentRegistrar : BaseSpecCompilerPluginRegistrar<PluginComponentRegistrar.Spec>() {
    override val supportsK2: Boolean
        get() = true

    data class Spec(
        val writeSpec: ExportWriteSpec?,
        val warnOnExported: Boolean,
    )

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun produceSpec(configuration: CompilerConfiguration): Spec {
        // Read from kcp-development options; enforce required semantics as before
        val outputFile = configuration[SymbolExportOptions.outputFile]!!
        val projectName = configuration[SymbolExportOptions.projectName]!!
        val projectGroup = configuration[SymbolExportOptions.projectGroup]!!
        val projectArtifact = configuration[SymbolExportOptions.projectArtifact]!!
        val projectVersion = configuration[SymbolExportOptions.projectVersion]!!
        val sourceSetName = configuration[SymbolExportOptions.sourceSetName]!!
        val warnOnExported = configuration[SymbolExportOptions.warnOnExported]

        return Spec(
            ExportWriteSpec(
                outputFile,
                projectName,
                ProjectCoordinates(projectGroup, projectArtifact, projectVersion),
                sourceSetName,
            ),
            warnOnExported
        )
    }

    override fun firExtension(spec: Spec) = PluginRegistrar(spec.writeSpec, spec.warnOnExported)

    override fun irExtension(spec: Spec) = null
}