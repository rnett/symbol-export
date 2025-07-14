package dev.rnett.symbolexport.symbol

public interface NameLike {
    public val nameSegments: List<String>
    public fun asString(): String = nameSegments.joinToString(".")

    public operator fun plus(other: NameSegments): NameSegments
    public operator fun plus(segment: String): NameSegments = plus(NameSegments(segment))
    public fun resolve(vararg segments: String): NameSegments = plus(NameSegments(*segments))
}

public data class NameSegments(override val nameSegments: List<String>) : NameLike {
    public constructor(vararg segments: String) : this(segments.toList())

    init {
        if (nameSegments.any { '.' in it })
            throw IllegalArgumentException("Segment has illegal character: '.'")
    }

    public override operator fun plus(other: NameSegments): NameSegments = NameSegments(nameSegments + other.nameSegments)
    public override operator fun plus(segment: String): NameSegments = NameSegments(nameSegments + segment)
    public override fun resolve(vararg segments: String): NameSegments = NameSegments(this.nameSegments + segments)
}

public enum class ParameterKind {
    DISPATCH_RECEIVER, EXTENSION_RECEIVER, CONTEXT, VALUE;
}

public sealed interface Symbol : NameLike {

    public val fullName: NameSegments
    override val nameSegments: List<String>
        get() = fullName.nameSegments

    override fun asString(): String = fullName.asString()
    override fun plus(other: NameSegments): NameSegments = fullName + other

    public sealed interface TypeParamHost : Symbol

    /**
     * A class, interface, object, etc.
     */
    public data class Classifier(val packageName: NameSegments, val classNames: NameSegments) :
        Symbol, TypeParamHost {
        override val fullName: NameSegments = packageName + classNames
    }

    public sealed interface Member : Symbol, TypeParamHost {
        public val name: String
    }

    public data class ClassifierMember(val classifier: Classifier, override val name: String) : Member {
        override val fullName: NameSegments = classifier + name
    }

    public data class Constructor(val classifier: Classifier, override val name: String) : Member {
        override val fullName: NameSegments = classifier + name
    }

    public data class TopLevelMember(val packageName: NameSegments, override val name: String) : Member {
        override val fullName: NameSegments = packageName + name
    }

    public data class TypeParameter(val owner: TypeParamHost, val index: Int, val name: String) : Symbol {
        override val fullName: NameSegments = owner + name
    }

    public sealed class Parameter(public val kind: ParameterKind) : Symbol {
        public abstract val name: String
        public abstract val owner: Member
        override val fullName: NameSegments by lazy { owner + name }

        /**
         * The index according to the Kotlin compiler's parameter ordering: `[dispatch receiver, context parameters, extension receiver, value parameters]`.
         */
        public abstract val index: Int
    }

    public data class ValueParameter(
        override val owner: Member,
        override val index: Int,
        val indexInValueParameters: Int,
        override val name: String
    ) : Parameter(ParameterKind.VALUE)

    public data class ContextParameter(
        override val owner: Member,
        override val index: Int,
        val indexInContextParameters: Int,
        override val name: String
    ) : Parameter(ParameterKind.CONTEXT)

    public data class ExtensionReceiverParameter(
        override val owner: Member,
        override val index: Int,
        override val name: String
    ) : Parameter(ParameterKind.EXTENSION_RECEIVER)

    public data class DispatchReceiverParameter(
        override val owner: Member,
        override val index: Int,
        override val name: String
    ) : Parameter(ParameterKind.DISPATCH_RECEIVER)

    public data class EnumEntry(val enumClass: Classifier, val entryName: String, val entryOrdinal: Int) : Symbol {
        override val fullName: NameSegments = enumClass + entryName
    }
}