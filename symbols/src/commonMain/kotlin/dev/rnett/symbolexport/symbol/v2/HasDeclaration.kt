package dev.rnett.symbolexport.symbol.v2

import dev.rnett.symbolexport.symbol.SymbolExportInternals

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class HasDeclaration<out T : SymbolDeclaration>(private val declaration: T) {
    public operator fun invoke(): T = declaration
}