package com.rnett.symbolexport

import org.gradle.api.attributes.Usage
import java.io.Serializable

internal object Shared {
    data object SymbolExportUsage : Usage, Serializable {
        private fun readResolve(): Any = SymbolExportUsage
        override fun getName(): String = "symbol-export"

    }

    val USAGE_ATTRIBUTE_VALUE = SymbolExportUsage
    val SYMBOLS_FILE_EXTENSION = ".symbols.json"
    val SYMBOLS_FILE_PREFIX = "exported-symbols-"
}