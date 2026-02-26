package dev.rnett.symbolexport.symbol.v2

import dev.rnett.symbolexport.symbol.SymbolExportInternals

public sealed interface Declaration : SymbolDeclaration {
}

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class ClassDeclaration(symbol: ClassSymbol) : Declaration, ClassSymbolDeclaration(symbol)

public sealed interface CallableDeclaration : Declaration, CallableSymbolDeclaration {
    abstract override val symbol: CallableSymbol
}

public sealed interface FunctionDeclaration : Declaration, FunctionSymbolDeclaration {
    abstract override val symbol: FunctionSymbol
}

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class SimpleFunctionDeclaration(symbol: SimpleFunctionSymbol) : FunctionDeclaration, SimpleFunctionSymbolDeclaration(symbol)

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class ConstructorDeclaration(symbol: ConstructorSymbol) : FunctionDeclaration, ConstructorSymbolDeclaration(symbol)

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class PropertyDeclaration(symbol: PropertySymbol) : CallableDeclaration, PropertySymbolDeclaration(symbol)

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class EnumEntryDeclaration(symbol: EnumEntrySymbol) : Declaration, EnumEntrySymbolDeclaration(symbol)