package dev.rnett.symbolexport.symbol.v2

import dev.rnett.symbolexport.symbol.SymbolExportInternals

public sealed interface SymbolDeclaration {
    public val symbol: Symbol
}

public sealed class ClassSymbolDeclaration(override val symbol: ClassSymbol) : SymbolDeclaration {

    @SymbolExportInternals
    public class Impl(symbol: ClassSymbol) : ClassSymbolDeclaration(symbol)

    override fun equals(other: Any?): Boolean = other is ClassSymbolDeclaration && symbol == other.symbol
    override fun hashCode(): Int = symbol.hashCode()
    override fun toString(): String = symbol.toString()
}

public sealed interface CallableSymbolDeclaration : SymbolDeclaration {
    abstract override val symbol: CallableSymbol
}

public sealed interface FunctionSymbolDeclaration : CallableSymbolDeclaration {
    abstract override val symbol: FunctionSymbol
}

public sealed class SimpleFunctionSymbolDeclaration(override val symbol: SimpleFunctionSymbol) : FunctionSymbolDeclaration {

    @SymbolExportInternals
    public class Impl(symbol: SimpleFunctionSymbol) : SimpleFunctionSymbolDeclaration(symbol)

    override fun equals(other: Any?): Boolean = other is SimpleFunctionSymbolDeclaration && symbol == other.symbol
    override fun hashCode(): Int = symbol.hashCode()
    override fun toString(): String = symbol.toString()
}

public sealed class ConstructorSymbolDeclaration(override val symbol: ConstructorSymbol) : FunctionSymbolDeclaration {

    @SymbolExportInternals
    public class Impl(symbol: ConstructorSymbol) : ConstructorSymbolDeclaration(symbol)

    override fun equals(other: Any?): Boolean = other is ConstructorSymbolDeclaration && symbol == other.symbol
    override fun hashCode(): Int = symbol.hashCode()
    override fun toString(): String = symbol.toString()
}

public sealed class PropertySymbolDeclaration(override val symbol: PropertySymbol) : CallableSymbolDeclaration {

    @SymbolExportInternals
    public class Impl(symbol: PropertySymbol) : PropertySymbolDeclaration(symbol)

    override fun equals(other: Any?): Boolean = other is PropertySymbolDeclaration && symbol == other.symbol
    override fun hashCode(): Int = symbol.hashCode()
    override fun toString(): String = symbol.toString()
}

public sealed class EnumEntrySymbolDeclaration(override val symbol: EnumEntrySymbol) : SymbolDeclaration {

    @SymbolExportInternals
    public class Impl(symbol: EnumEntrySymbol) : EnumEntrySymbolDeclaration(symbol)

    override fun equals(other: Any?): Boolean = other is EnumEntrySymbolDeclaration && symbol == other.symbol
    override fun hashCode(): Int = symbol.hashCode()
    override fun toString(): String = symbol.toString()
}