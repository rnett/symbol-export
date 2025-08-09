package dev.rnett.symbolexport.symbol.annotation

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument.EnumEntry

/**
 * An annotation parameter definition.
 */
public data class AnnotationParameter<P : AnnotationParameterType<*>>(val name: String, val index: Int, val type: P)

/**
 * The type of an annotation parameter.
 */
public sealed interface AnnotationParameterType<V : AnnotationArgument> {

    public data object KClass : AnnotationParameterType<AnnotationArgument.KClass>
    public data class Enum(val enumClass: Symbol.Classifier) : AnnotationParameterType<AnnotationArgument.EnumEntry>
    public data class Annotation<S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>>(val annotationClass: S) : AnnotationParameterType<AnnotationArgument.Annotation<S, I>>
    public data class Array<T : AnnotationParameterType<E>, E : AnnotationArgument>(val elementType: T) : AnnotationParameterType<AnnotationArgument.Array<E>>

    public sealed class Primitive<T : Any, A : AnnotationArgument.Primitive<T>>(public val kClass: kotlin.reflect.KClass<T>) : AnnotationParameterType<A> {
        public abstract fun createArgument(value: T): A
    }

    public data object String : Primitive<kotlin.String, AnnotationArgument.String>(kotlin.String::class) {
        override fun createArgument(value: kotlin.String): AnnotationArgument.String {
            return AnnotationArgument.String(value)
        }
    }

    public data object Boolean : Primitive<kotlin.Boolean, AnnotationArgument.Boolean>(kotlin.Boolean::class) {
        override fun createArgument(value: kotlin.Boolean): AnnotationArgument.Boolean {
            return AnnotationArgument.Boolean(value)
        }
    }

    public data object Int : Primitive<kotlin.Int, AnnotationArgument.Int>(kotlin.Int::class) {
        override fun createArgument(value: kotlin.Int): AnnotationArgument.Int {
            return AnnotationArgument.Int(value)
        }
    }

    public data object Float : Primitive<kotlin.Float, AnnotationArgument.Float>(kotlin.Float::class) {
        override fun createArgument(value: kotlin.Float): AnnotationArgument.Float {
            return AnnotationArgument.Float(value)
        }
    }

    public data object Long : Primitive<kotlin.Long, AnnotationArgument.Long>(kotlin.Long::class) {
        override fun createArgument(value: kotlin.Long): AnnotationArgument.Long {
            return AnnotationArgument.Long(value)
        }
    }

    public data object Double : Primitive<kotlin.Double, AnnotationArgument.Double>(kotlin.Double::class) {
        override fun createArgument(value: kotlin.Double): AnnotationArgument.Double {
            return AnnotationArgument.Double(value)
        }
    }

    public data object Char : Primitive<kotlin.Char, AnnotationArgument.Char>(kotlin.Char::class) {
        override fun createArgument(value: kotlin.Char): AnnotationArgument.Char {
            return AnnotationArgument.Char(value)
        }
    }

    public data object Byte : Primitive<kotlin.Byte, AnnotationArgument.Byte>(kotlin.Byte::class) {
        override fun createArgument(value: kotlin.Byte): AnnotationArgument.Byte {
            return AnnotationArgument.Byte(value)
        }
    }

    public data object Short : Primitive<kotlin.Short, AnnotationArgument.Short>(kotlin.Short::class) {
        override fun createArgument(value: kotlin.Short): AnnotationArgument.Short {
            return AnnotationArgument.Short(value)
        }
    }
}

/**
 * An argument of an annotation instance.
 */
public sealed class AnnotationArgument(public open val type: AnnotationParameterType<*>) {

    /**
     * An array argument with elements of [elementType].
     */
    public data class Array<T : AnnotationArgument>(val values: List<T>, val elementType: AnnotationParameterType<T>) : AnnotationArgument(AnnotationParameterType.Array(elementType)), List<T> by values {
        public constructor(values: List<T>) : this(values.toList(), Unit.run {
            require(values.isNotEmpty()) { "Element type must be specified when creating an empty array" }
            val types = values.mapTo(mutableSetOf()) { it.type }
            require(types.size == 1) { "All elements of an array must have the same type, but got $types" }

            @Suppress("UNCHECKED_CAST")
            types.single() as AnnotationParameterType<T>
        })

        init {
            require(values.all { it.type == elementType }) { "All elements of an array must be of the array's element type ($elementType), but got ${values.map { it.type }}" }
        }
    }

    /**
     * An enum argument. Does not include the ordinal because that is not always available.
     */
    public data class EnumEntry(val enumClass: Symbol.Classifier, val enumName: kotlin.String) : AnnotationArgument(AnnotationParameterType.Enum(enumClass)) {
        public constructor(enumEntry: Symbol.EnumEntry) : this(
            enumEntry.enumClass,
            enumEntry.name
        )
    }

    /**
     * A class literal argument.
     */
    public data class KClass(val classSymbol: Symbol.Classifier) : AnnotationArgument(AnnotationParameterType.KClass)

    /**
     * Another annotation used as an argument.
     */
    public data class Annotation<S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>>(val annotationArguments: I) : AnnotationArgument(AnnotationParameterType.Annotation(annotationArguments.annotation))

    /**
     * An argument of one of the primitive types (+ String).
     */
    public sealed class Primitive<T : Any>(override val type: AnnotationParameterType.Primitive<T, *>) : AnnotationArgument(type) {
        public abstract val value: T
    }

    public data class String(override val value: kotlin.String) : Primitive<kotlin.String>(AnnotationParameterType.String)
    public data class Boolean(override val value: kotlin.Boolean) : Primitive<kotlin.Boolean>(AnnotationParameterType.Boolean)
    public data class Int(override val value: kotlin.Int) : Primitive<kotlin.Int>(AnnotationParameterType.Int)
    public data class Float(override val value: kotlin.Float) : Primitive<kotlin.Float>(AnnotationParameterType.Float)
    public data class Long(override val value: kotlin.Long) : Primitive<kotlin.Long>(AnnotationParameterType.Long)
    public data class Double(override val value: kotlin.Double) : Primitive<kotlin.Double>(AnnotationParameterType.Double)
    public data class Char(override val value: kotlin.Char) : Primitive<kotlin.Char>(AnnotationParameterType.Char)
    public data class Byte(override val value: kotlin.Byte) : Primitive<kotlin.Byte>(AnnotationParameterType.Byte)
    public data class Short(override val value: kotlin.Short) : Primitive<kotlin.Short>(AnnotationParameterType.Short)

    public companion object {
        public fun kClass(value: Symbol.ClassLike): KClass = KClass(value.asClassifier())

        /**
         * Create an array argument. All elements must be of type [elementType].
         *
         * @throws IllegalArgumentException if any elements are not of type [elementType]
         */
        public fun <T : AnnotationArgument> array(value: List<T>, elementType: AnnotationParameterType<T>): Array<T> = Array(value, elementType)

        /**
         * Create an array argument. All elements must have the same [AnnotationArgument.type]. Passing an empty list to this function is an error.
         *
         * @throws IllegalArgumentException if there are no elements, or if any elements have a different type
         */
        public fun <T : AnnotationArgument> nonEmptyArray(value: List<T>): Array<T> = Array(value)

        /**
         * Create an array argument. All elements must have the same [AnnotationArgument.type].
         *
         * @throws IllegalArgumentException if any elements have a different type
         */
        public fun <T : AnnotationArgument> array(first: T, vararg values: T): Array<T> = Array(buildList {
            add(first)
            addAll(values)
        })

        public fun enum(value: Symbol.EnumEntry): EnumEntry = EnumEntry(value)
        public fun enum(value: Symbol.Classifier, name: kotlin.String): EnumEntry = EnumEntry(value, name)

        public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> annotation(value: I): Annotation<S, I> = Annotation(value)

        public fun of(value: kotlin.String): String = String(value)
        public fun of(value: kotlin.Boolean): Boolean = Boolean(value)
        public fun of(value: kotlin.Int): Int = Int(value)
        public fun of(value: kotlin.Float): Float = Float(value)
        public fun of(value: kotlin.Long): Long = Long(value)
        public fun of(value: kotlin.Double): Double = Double(value)
        public fun of(value: kotlin.Char): Char = Char(value)
        public fun of(value: kotlin.Byte): Byte = Byte(value)
        public fun of(value: kotlin.Short): Short = Short(value)
    }
}

@Suppress("UNCHECKED_CAST")
public fun <A : AnnotationArgument, T : AnnotationParameterType<A>> AnnotationArgument.asTypeOrNull(type: T): A? {
    if (this.type != type)
        return null
    return this as A
}

public fun <A : AnnotationArgument> AnnotationArgument.asType(type: AnnotationParameterType<A>): A = asTypeOrNull(type)
    ?: throw IllegalArgumentException("Expected argument with type $type, got $this")

public fun Symbol.EnumEntry.asAnnotationArgument(): EnumEntry = EnumEntry(enumClass, name)
public fun Symbol.Classifier.asAnnotationArgument(): AnnotationArgument.KClass = AnnotationArgument.kClass(this)
public fun Symbol.Annotation<*, *>.classAsAnnotationArgument(): AnnotationArgument.KClass = AnnotationArgument.kClass(this)
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> I.asAnnotationArgument(): AnnotationArgument.Annotation<S, I> = AnnotationArgument.annotation(this)

