package dev.rnett.symbolexport.symbol.v2

import dev.rnett.symbolexport.symbol.SymbolExportInternals

/**
 * A function's signature for overload resolution.
 *
 * Used by integration libraries, you probably don't want to use this yourself.
 * @suppress
 */
@SymbolExportInternals
public data class FunctionSignature(val params: List<ParamSignature>) {
    /**
     * The signature of a parameter.
     * @suppress
     */
    @SymbolExportInternals
    public data class ParamSignature(val name: String, val hasDefaultValue: Boolean, val type: TypeSignature) {

    }

    /**
     * The signature of a type.
     */
    @SymbolExportInternals
    public sealed interface TypeSignature {
        public data class ClassBased(val classifierFqn: String, val isNullable: Boolean, val arguments: List<TypeArgumentSignature>) : TypeSignature
        public data class TypeParam(val name: String, val isNullable: Boolean) : TypeSignature
        public data object Dynamic : TypeSignature {}
    }

    /**
     * The signature of a type argument.
     */
    @SymbolExportInternals
    public sealed interface TypeArgumentSignature {
        public data class Projection(val type: TypeSignature, val variance: Variance) : TypeArgumentSignature
        public data object Wildcard : TypeArgumentSignature

        public enum class Variance {
            INVARIANT, IN, OUT
        }
    }
}