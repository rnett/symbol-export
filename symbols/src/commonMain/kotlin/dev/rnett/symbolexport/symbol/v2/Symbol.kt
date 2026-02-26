package dev.rnett.symbolexport.symbol.v2

import dev.rnett.symbolexport.symbol.SymbolExportInternals

public sealed class Symbol {
    public abstract val qualifiedName: QualifiedName
    public val name: String get() = qualifiedName.name

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Symbol) return false
        return this::class == other::class && qualifiedName == other.qualifiedName
    }

    override fun hashCode(): Int {
        return this::class.simpleName.hashCode() + 31 * qualifiedName.hashCode()
    }

    protected abstract fun kindToString(): String

    override fun toString(): String = "[${kindToString()}] $qualifiedName"
}

public class ClassSymbol(override val qualifiedName: QualifiedName.ClassName) : Symbol() {
    override fun kindToString(): String = "class"
}

public sealed class CallableSymbol : Symbol() {
    public abstract override val qualifiedName: QualifiedName.CallableName
}

public sealed class FunctionSymbol(
    /**
     * The function's signature for overload resolution.
     *
     * Used by integration libraries, you probably don't want to use this yourself.
     *
     * @suppress
     */
    @property:SymbolExportInternals
    public val signature: FunctionSignature
) : CallableSymbol() {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is FunctionSymbol) return false
        if (!super.equals(other)) return false
        return signature == other.signature
    }

    override fun hashCode(): Int {
        return super.hashCode() + 31 * signature.hashCode()
    }

    override fun toString(): String = "$qualifiedName(${signature.params.joinToString { it.name }})"
}

public class SimpleFunctionSymbol(
    override val qualifiedName: QualifiedName.CallableName,
    signature: FunctionSignature
) : FunctionSymbol(signature) {
    override fun kindToString(): String = "fun"
}

public class ConstructorSymbol(
    override val qualifiedName: QualifiedName.MemberName,
    signature: FunctionSignature
) : FunctionSymbol(signature) {
    override fun kindToString(): String = "constructor"
}

public class PropertySymbol(override val qualifiedName: QualifiedName.CallableName) : CallableSymbol() {
    override fun kindToString(): String = "property"
}

public class EnumEntrySymbol(
    override val qualifiedName: QualifiedName.MemberName
) : Symbol() {

    public val className: QualifiedName.ClassName get() = qualifiedName.className
    override fun kindToString(): String = "enum entry"
}