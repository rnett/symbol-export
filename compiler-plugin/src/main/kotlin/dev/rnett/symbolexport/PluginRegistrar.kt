package dev.rnett.symbolexport

import dev.rnett.symbolexport.fir.SymbolExportCheckerExtension
import dev.rnett.symbolexport.internal.ProjectCoordinates
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import java.nio.file.Path

data class ExportWriteSpec(
    val outputFile: Path,
    val projectName: String,
    val projectCoordinates: ProjectCoordinates,
    val sourceSetName: String,
)

class PluginRegistrar() : FirExtensionRegistrar() {

    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirAdditionalCheckersExtension.Factory {
            SymbolExportCheckerExtension(it)
        }
    }
}