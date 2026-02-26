package dev.rnett.symbolexport

import dev.rnett.kcp.development.options.get
import dev.rnett.kcp.development.registrar.BaseSpecCompilerPluginRegistrar
import dev.rnett.`symbol-export`.BuildConfig
import dev.rnett.symbolexport.ir.IrExtension
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.nio.file.Path

@OptIn(ExperimentalCompilerApi::class)
class PluginComponentRegistrar : BaseSpecCompilerPluginRegistrar<PluginComponentRegistrar.Spec>() {
    override val supportsK2: Boolean
        get() = true

    data class Spec(
        val warnOnExported: Boolean,
        val outputDir: Path?,
        val rootPath: Path?
    )

    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun produceSpec(configuration: CompilerConfiguration): Spec {
        // Read from kcp-development options; enforce required semantics as before
        val outputDir = configuration[SymbolExportOptions.outputDir]!!
        val projectName = configuration[SymbolExportOptions.projectName]!!
        val projectGroup = configuration[SymbolExportOptions.projectGroup]!!
        val projectArtifact = configuration[SymbolExportOptions.projectArtifact]!!
        val projectVersion = configuration[SymbolExportOptions.projectVersion]!!
        val sourceSetName = configuration[SymbolExportOptions.sourceSetName]!!
        val rootDir = configuration[SymbolExportOptions.rootDir]
        val warnOnExported = configuration[SymbolExportOptions.warnOnExported]

        return Spec(
            warnOnExported,
            outputDir,
            rootDir //TODO gradle project root
        )
    }

    override fun firExtension(spec: Spec) = PluginRegistrar()

    override fun irExtension(spec: Spec) = IrExtension(spec)
}