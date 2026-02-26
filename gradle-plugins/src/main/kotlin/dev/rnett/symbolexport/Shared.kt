package dev.rnett.symbolexport

internal object Shared {

    const val USAGE_ATTRIBUTE_VALUE = "symbol-export"
    const val EXPORTED_SYMBOLS_FILENAME = "exported-symbols.jsonl"
    const val FEATURE_VALUE = "exported-symbols"

    fun getSymbolCapability(group: String, name: String, version: String) =
        "$group:$name-exported-symbols:$version"
}