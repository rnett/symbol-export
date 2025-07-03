package dev.rnett.symbolexport

import org.gradle.api.attributes.Usage
import java.io.Serializable

internal object Shared {
    data object SymbolExportUsage : Usage, Serializable {
        private fun readResolve(): Any = SymbolExportUsage
        override fun getName(): String = "symbol-export"

    }

    val USAGE_ATTRIBUTE_VALUE = SymbolExportUsage
    val EXPORTED_SYMBOLS_FILENAME = "exported-symbols.jsonl"
}