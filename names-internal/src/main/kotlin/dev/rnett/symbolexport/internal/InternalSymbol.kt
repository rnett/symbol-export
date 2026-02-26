package dev.rnett.symbolexport.internal

import kotlinx.serialization.Serializable

@Serializable
public sealed interface InternalSymbol {
    public val exportedName: String? get() = null

    public val qualifiedNameSegments: List<String>

    /**
     * A class, interface, object, etc.
     */
    @Serializable
    public data class Classifier(
        val packageNames: List<String>,
        val classNames: List<String>
    ) : InternalSymbol {
        override val qualifiedNameSegments: List<String>
            get() = packageNames + classNames
    }

    @Serializable
    public sealed interface Member : InternalSymbol {
        public val packageNames: List<String>
        public val classNames: List<String>?
        public val name: String
    }


    @Serializable
    public data class Property(
        override val packageNames: List<String>,
        override val classNames: List<String>?,
        override val name: String,
        override val exportedName: String?,
    ) : Member {
        override val qualifiedNameSegments: List<String>
            get() = packageNames + classNames.orEmpty() + name
    }

    @Serializable
    public data class Function(
        override val packageNames: List<String>,
        override val classNames: List<String>?,
        override val name: String,
        // necessary for overload resolution
        val parameterSignatures: List<ParameterSignature>,
        override val exportedName: String?,
    ) : Member {
        override val qualifiedNameSegments: List<String>
            get() = packageNames + classNames.orEmpty() + name
    }

    @Serializable
    public data class ParameterSignature(
        val name: String,
        val hasDefaultValue: Boolean,
        val type: ParamType
    )

    @Serializable
    public sealed interface ParamType {
        @Serializable
        public data class ClassBased(val classifierFqn: String, val isNullable: Boolean, val arguments: List<ParamTypeArg>) : ParamType

        @Serializable
        public data class TypeParam(val name: String, val isNullable: Boolean) : ParamType

        @Serializable
        public data object Dynamic : ParamType
    }

    @Serializable
    public sealed interface ParamTypeArg {
        @Serializable
        public data object Wildcard : ParamTypeArg

        @Serializable
        public data class TypeProjection(
            val variance: Variance,
            val type: ParamType
        ) : ParamTypeArg

        @Serializable
        public enum class Variance {
            INVARIANT,
            IN,
            OUT
        }
    }

    @Serializable
    public data class EnumEntry(val owner: Classifier, val name: String, val ordinal: Int) : InternalSymbol {
        override val qualifiedNameSegments: List<String>
            get() = owner.qualifiedNameSegments + name
    }
}