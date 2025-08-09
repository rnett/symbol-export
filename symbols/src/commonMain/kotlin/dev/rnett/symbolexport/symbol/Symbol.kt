package dev.rnett.symbolexport.symbol

import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameter
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType

/**
 * A type that has a name composed of `.`-seperated segments.
 */
public interface NameLike {
    /**
     * The segments of the name.
     */
    public val nameSegments: List<String>

    /**
     * The name as a string.
     */
    public fun asString(): String = nameSegments.joinToString(".")

    /**
     * Resolves [other] after this.
     */
    public operator fun plus(other: NameSegments): NameSegments

    /**
     * Resolves [segment] after this.
     */
    public operator fun plus(segment: String): NameSegments = plus(NameSegments(segment))

    /**
     * Resolves [segments] after this.
     */
    public fun resolve(vararg segments: String): NameSegments = plus(NameSegments(*segments))
}

/**
 * A name composed of `.`-seperated segments.
 */
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

/**
 * The kind of a [Symbol.Parameter].
 */
public enum class ParameterKind {
    DISPATCH_RECEIVER, EXTENSION_RECEIVER, CONTEXT, VALUE;
}

/**
 * A referenced Kotlin symbol.
 */
public sealed interface Symbol : NameLike {

    /**
     * The fully qualified name of the symbol.
     */
    public val fullName: NameSegments

    /**
     * The segments of the fully qualified name of this symbol.
     */
    override val nameSegments: List<String>
        get() = fullName.nameSegments

    /**
     * The fully qualified name as a string.
     */
    override fun asString(): String = fullName.asString()
    override fun plus(other: NameSegments): NameSegments = fullName + other

    /**
     * A symbol that can have type parameters.
     */
    public sealed interface TypeParamHost : Symbol

    /**
     * A symbol with a meaningful simple name. This is most symbols except [Constructor].
     */
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
        /**
         * The package name of the classifier.
         */
        public val packageName: NameSegments

        /**
         * The class names of the classifier.
         */
        public val classNames: NameSegments

        override val fullName: NameSegments get() = packageName + classNames

        override val name: String get() = classNames.nameSegments.last()

        public fun asClassifier(): Classifier = this as? Classifier ?: Classifier(packageName, classNames)
    }

    /**
     * A class, interface, object, etc.
     * Annotations may either be exported as a classifier (if `@ExportSymbol` is used) or as an [Annotation] (if `@ExportAnnotation` is used).
     */
    public data class Classifier(override val packageName: NameSegments, override val classNames: NameSegments) :
        ClassLike, TypeParamHost {
    }

    /**
     * A member. Either of a [Classifier] or as a top-level symbol.
     */
    public sealed interface Member : Symbol, TypeParamHost {
    }

    /**
     * A member with a meaningful name.
     */
    public sealed interface NamedMember : Member, NamedSymbol

    /**
     * A named [Member] of a [Classifier].
     *
     * @property classifier The classifier that contains the member
     */
    public data class NamedClassifierMember(val classifier: Classifier, override val name: String) : NamedMember {
        override val fullName: NameSegments = classifier + name
    }

    /**
     * A constructor of a [Classifier].
     *
     * Uses `<init>` as its name in its [fullName] to avoid collision with classes.
     *
     * @property classifier The classifier that contains the constructor
     */
    public data class Constructor(val classifier: Classifier) : Member {
        public companion object {
            public const val NAME: String = "<init>"
        }

        override val fullName: NameSegments = classifier.fullName + NAME
    }

    /**
     * A top-level member.
     *
     * @property packageName The package name of the member
     */
    public data class TopLevelMember(val packageName: NameSegments, override val name: String) : NamedMember {
        override val fullName: NameSegments = packageName + name
    }

    /**
     * A type parameter.
     *
     * @property owner The symbol that owns the type parameter
     * @property index The index of the type parameter in [owner]'s type parameters
     * @property name The name of the type parameter
     */
    public data class TypeParameter(val owner: TypeParamHost, val index: Int, override val name: String) : Symbol, NamedSymbol {
        override val fullName: NameSegments = owner + name
    }

    /**
     * A parameter of a [Member].
     *
     * @property kind The kind of the parameter
     * @property owner The symbol that owns the parameter
     * @property index The index of the parameter in all of [owner]'s parameters. Ordered according to the Kotlin compiler's parameter ordering: `[dispatch receiver, context parameters, extension receiver, value parameters]`.
     * @property name The name of the parameter
     */
    public sealed class Parameter(public val kind: ParameterKind) : Symbol, NamedSymbol {
        public abstract override val name: String
        public abstract val owner: Member
        override val fullName: NameSegments by lazy { owner + name }

        public abstract val index: Int
    }

    /**
     * A value parameter of a [Member].
     *
     * @property indexInValueParameters The index of the parameter in the value parameters of the member.
     * @see Parameter
     */
    public data class ValueParameter(
        override val owner: Member,
        override val index: Int,
        val indexInValueParameters: Int,
        override val name: String
    ) : Parameter(ParameterKind.VALUE)

    /**
     * A context parameter of a [Member].
     *
     * @property indexInContextParameters The index of the parameter in the context parameters of the member.
     * @see Parameter
     */
    public data class ContextParameter(
        override val owner: Member,
        override val index: Int,
        val indexInContextParameters: Int,
        override val name: String
    ) : Parameter(ParameterKind.CONTEXT)

    /**
     * The extension receiver parameter of a [Member].
     */
    public data class ExtensionReceiverParameter(
        override val owner: Member,
        override val index: Int,
        override val name: String
    ) : Parameter(ParameterKind.EXTENSION_RECEIVER)

    /**
     * The dispatch receiver parameter of a [Member].
     */
    public data class DispatchReceiverParameter(
        override val owner: Member,
        override val index: Int,
        override val name: String
    ) : Parameter(ParameterKind.DISPATCH_RECEIVER)

    /**
     * An enum entry.
     */
    public data class EnumEntry(val enumClass: Classifier, val entryName: String, val entryOrdinal: Int) : Symbol, NamedSymbol {
        override val fullName: NameSegments = enumClass + entryName
        override val name: String = entryName
    }

    /**
     * A full representation of an annotation, as opposed to [Classifier] which just represents the type.
     * Includes all parameters and the ability to constructor [Instance]s for a given set of arguments.
     *
     * @property packageName The package name of the annotation
     * @property classNames The class names of the annotation
     * @property parameters All parameters of the annotation
     */
    public abstract class Annotation<S : Annotation<S, I>, I : Annotation.Instance<S, I>>(
        override val packageName: NameSegments,
        override val classNames: NameSegments
    ) : ClassLike {
        /**
         * A representation of an annotation instance.
         *
         * @property annotation The annotation type
         * @property arguments The arguments of the annotation instance, keyed by the parameter name. All parameters are present as keys - if they are not specified, the value is null.
         */
        public abstract class Instance<S : Annotation<S, I>, I : Instance<S, I>> {
            public abstract val annotation: S
            public abstract val arguments: Map<AnnotationParameter<*>, AnnotationArgument?>

            public inline operator fun <reified T : AnnotationArgument, P : AnnotationParameterType<T>> get(param: AnnotationParameter<P>): T? = arguments[param] as T?
            public operator fun contains(param: AnnotationParameter<*>): Boolean = param in arguments

            public operator fun get(param: String): AnnotationArgument? = arguments.entries.firstOrNull { it.key.name == param }?.value
            public operator fun contains(param: String): Boolean = arguments.keys.any { it.name == param }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as Instance<*, *>

                if (annotation != other.annotation) return false
                if (arguments != other.arguments) return false

                return true
            }

            override fun hashCode(): Int {
                var result = annotation.hashCode()
                result = 31 * result + arguments.hashCode()
                return result
            }

            override fun toString(): String {
                return "Annotation.Instance(annotation=${annotation.asString()}, arguments=${arguments.mapKeys { it.key.name }})"
            }

        }

        public abstract val parameters: List<AnnotationParameter<*>>

        /**
         * Creates an [Instance] of the annotation by reading an argument for each parameter using [producer].
         */
        public abstract fun produceInstance(producer: AnnotationArgumentProducer): I

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Annotation<*, *>

            if (packageName != other.packageName) return false
            if (classNames != other.classNames) return false
            if (parameters != other.parameters) return false

            return true
        }

        override fun hashCode(): Int {
            var result = packageName.hashCode()
            result = 31 * result + classNames.hashCode()
            result = 31 * result + parameters.hashCode()
            return result
        }

        override fun toString(): String {
            return "Annotation(packageName=$packageName, classNames=$classNames, parameters=$parameters)"
        }
    }
}