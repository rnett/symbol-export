package dev.rnett.symbolexport

import dev.rnett.symbolexport.fir.SymbolExportCheckerExtension
import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalNameEntry
import dev.rnett.symbolexport.internal.ProjectCoordinates
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import java.nio.file.Path
import kotlin.io.path.*

data class ExportWriteSpec(
    val outputFile: Path,
    val projectName: String,
    val projectCoordinates: ProjectCoordinates,
    val sourceSetName: String,
)

class PluginRegistrar(
    val writeSpec: ExportWriteSpec?,
    val warnOnExported: Boolean,
) :
    FirExtensionRegistrar() {
    val json = Json {}

    init {
        writeSpec?.apply {
            if (outputFile.exists()) {
                if (outputFile.isDirectory()) {
                    outputFile.deleteExisting()
                    outputFile.createFile()
                } else {
                    outputFile.writeText("")
                }
            } else {
                outputFile.createParentDirectories()
                outputFile.createFile()
            }
        }
    }

    fun writeDeclaration(name: InternalName) {
        synchronized(this) {
            writeSpec?.apply {
                outputFile.appendText(
                    json.encodeToString(
                        InternalNameEntry(
                            projectName,
                            projectCoordinates,
                            sourceSetName,
                            name
                        )
                    ) + "\n"
                )
            }
        }
    }

    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirAdditionalCheckersExtension.Factory {
            SymbolExportCheckerExtension(it, warnOnExported, this@PluginRegistrar::writeDeclaration)
        }
    }
}