package dev.rnett.symbolexport.internal

import kotlinx.serialization.Serializable

@Serializable
public sealed interface InternalDeclaration {
    public val symbol: InternalSymbol

    @Serializable
    public data class Classifier(
        override val symbol: InternalSymbol.Classifier,
        val typeParams: List<TypeParameter>,
        val annotationInfo: AnnotationInfo?,
        val enumInfo: EnumInfo?,
        val isConcrete: Boolean,
        val isObject: Boolean,
    ) : InternalDeclaration

    @Serializable
    public sealed interface Callable : InternalDeclaration {
        public val typeParams: List<TypeParameter>
        public val parameters: List<Parameter>
    }

    @Serializable
    public sealed interface Function : Callable

    @Serializable
    public data class SimpleFunction(
        override val symbol: InternalSymbol.Function,
        override val typeParams: List<TypeParameter>,
        override val parameters: List<Parameter>,
        val isAccessor: Boolean,
    ) : Function

    @Serializable
    public data class Constructor(
        override val symbol: InternalSymbol.Function,
        val classTypeParams: List<TypeParameter>,
        override val parameters: List<Parameter>,
    ) : Function {
        override val typeParams: List<TypeParameter>
            get() = emptyList()
    }

    @Serializable
    public data class Property(
        override val symbol: InternalSymbol,
        val getter: SimpleFunction,
        val setter: SimpleFunction?
    ) : Callable {
        override val typeParams: List<TypeParameter>
            get() = getter.typeParams
        override val parameters: List<Parameter>
            get() = getter.parameters
    }

    @Serializable
    public data class TypeParameter(val name: String, val index: Int)

    @Serializable
    public data class Parameter(
        val name: String,
        val index: Int,
        val kind: Kind
    ) {
        @Serializable
        public enum class Kind {
            DISPATCH,
            CONTEXT,
            EXTENSION,
            VALUE
        }
    }

    @Serializable
    public data class AnnotationInfo(val params: List<AnnotationParameter>)

    @Serializable
    public data class AnnotationParameter(val name: String, val index: Int, val type: AnnotationParameterType)

    @Serializable
    public data class EnumInfo(val entries: List<String>)
}