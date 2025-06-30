package com.rnett.symbolexport

import com.rnett.symbolexport.fir.SymbolExportCheckerExtension
import com.rnett.symbolexport.internal.InternalName
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import java.nio.file.Path
import kotlin.io.path.appendText
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.writeText

class PluginRegistrar(val outputFile: Path) : FirExtensionRegistrar() {
    val json = Json {}

    init {
        if (outputFile.exists()) {
            if (outputFile.isDirectory()) {
                outputFile.deleteExisting()
            }

            outputFile.writeText("")
        } else {
            outputFile.createParentDirectories()
            outputFile.createFile()
        }
    }

    fun writeDeclaration(name: InternalName) {
        outputFile.appendText(json.encodeToString(name) + "\n")
    }

    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirAdditionalCheckersExtension.Factory {
            SymbolExportCheckerExtension(it, this@PluginRegistrar::writeDeclaration)
        }
    }
}