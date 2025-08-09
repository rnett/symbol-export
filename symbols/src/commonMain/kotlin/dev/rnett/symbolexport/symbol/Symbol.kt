package dev.rnett.symbolexport.symbol

import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameter
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType

public interface NameLike {
    public val nameSegments: List<String>
    public fun asString(): String = nameSegments.joinToString(".")

    public operator fun plus(other: NameSegments): NameSegments
    public operator fun plus(segment: String): NameSegments = plus(NameSegments(segment))
    public fun resolve(vararg segments: String): NameSegments = plus(NameSegments(*segments))
}

internal object Test {
    object a : Any() {
        class Inner : Any() {
            fun test() = InnerInner()

            inner class InnerInner : Any()
        }
    }
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

    public sealed interface NamedSymbol : Symbol {
        /**
         * The simple name of this symbol. Usually the last segment in its fully qualified name.
         */
        public val name: String
    }

    /**
     * A [Classifier] or [Annotation]. May include type aliases someday.
     */
    public sealed interface ClassLike : Symbol, NamedSymbol {
        public val packageName: NameSegments
        public val classNames: NameSegments

        override val fullName: NameSegments get() = packageName + classNames

        override val name: String get() = classNames.nameSegments.last()
    }

    /**
     * A class, interface, object, etc.
     * Annotations may either be exported as a classifier (if `@ExportSymbol` is used) or as an [Annotation] (if `@ExportAnnotation` is used).
     */
    public data class Classifier(override val packageName: NameSegments, override val classNames: NameSegments) :
        ClassLike, TypeParamHost {
    }

    public sealed interface Member : Symbol, TypeParamHost, NamedSymbol {
        public override val name: String
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

    public data class TypeParameter(val owner: TypeParamHost, val index: Int, override val name: String) : Symbol, NamedSymbol {
        override val fullName: NameSegments = owner + name
    }

    public sealed class Parameter(public val kind: ParameterKind) : Symbol, NamedSymbol {
        public abstract override val name: String
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

    public data class EnumEntry(val enumClass: Classifier, val entryName: String, val entryOrdinal: Int) : Symbol, NamedSymbol {
        override val fullName: NameSegments = enumClass + entryName
        override val name: String = entryName
    }

    public abstract class Annotation<S : Annotation<S, A>, A : Annotation.Arguments<S, A>>(
        override val packageName: NameSegments,
        override val classNames: NameSegments
    ) : ClassLike {
        public interface Arguments<S : Annotation<S, A>, A : Arguments<S, A>> {
            public val annotation: S
            public val asMap: Map<AnnotationParameter<*>, AnnotationArgument?>

            public operator fun <T : AnnotationArgument, P : AnnotationParameterType<T>> get(param: AnnotationParameter<P>): T? = asMap[param] as T
            public operator fun contains(param: AnnotationParameter<*>): Boolean = param in asMap

            public operator fun get(param: String): AnnotationArgument? = asMap.entries.firstOrNull { it.key.name == param }?.value
            public operator fun contains(param: String): Boolean = asMap.keys.any { it.name == param }
        }

        public abstract val parameters: List<AnnotationParameter<*>>

        public abstract fun produceArguments(producer: AnnotationArgumentProducer): A
    }
}