package dev.rnett.symbolexport.symbol.annotation

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument.EnumEntry

public data class AnnotationParameter<P : AnnotationParameterType<*>>(val name: String, val type: P)

public interface AnnotationArgumentProducer {
    public fun <T : AnnotationArgument, P : AnnotationParameterType<T>> getArgument(parameter: AnnotationParameter<P>): T?
}

public sealed interface AnnotationParameterType<V : AnnotationArgument> {

    public data object KClass : AnnotationParameterType<AnnotationArgument.KClass>
    public data class Enum(val enumClass: Symbol.Classifier) : AnnotationParameterType<AnnotationArgument.EnumEntry>
    public data class Annotation<S : Symbol.Annotation<S, T>, T : Symbol.Annotation.Arguments<S, T>>(val annotationClass: S) : AnnotationParameterType<AnnotationArgument.Annotation<S, T>>
    public data class Array<T : AnnotationParameterType<E>, E : AnnotationArgument>(val elementType: T) : AnnotationParameterType<AnnotationArgument.Array<E>>

    public sealed class Primitive<T : Any, A : AnnotationArgument.Primitive<T>>(public val kClass: kotlin.reflect.KClass<T>) : AnnotationParameterType<A>

    public data object String : Primitive<kotlin.String, AnnotationArgument.String>(kotlin.String::class)
    public data object Boolean : Primitive<kotlin.Boolean, AnnotationArgument.Boolean>(kotlin.Boolean::class)
    public data object Int : Primitive<kotlin.Int, AnnotationArgument.Int>(kotlin.Int::class)
    public data object Float : Primitive<kotlin.Float, AnnotationArgument.Float>(kotlin.Float::class)
    public data object Long : Primitive<kotlin.Long, AnnotationArgument.Long>(kotlin.Long::class)
    public data object Double : Primitive<kotlin.Double, AnnotationArgument.Double>(kotlin.Double::class)
    public data object Char : Primitive<kotlin.Char, AnnotationArgument.Char>(kotlin.Char::class)
    public data object Byte : Primitive<kotlin.Byte, AnnotationArgument.Byte>(kotlin.Byte::class)
    public data object Short : Primitive<kotlin.Short, AnnotationArgument.Short>(kotlin.Short::class)
}

public sealed class AnnotationArgument(public open val type: AnnotationParameterType<*>) {

    public data class Array<T : AnnotationArgument>(val values: List<T>, val elementType: AnnotationParameterType<*>) : AnnotationArgument(AnnotationParameterType.Array(elementType)), List<T> by values {
        public constructor(values: List<T>) : this(values.toList(), Unit.run {
            require(values.isNotEmpty()) { "Element type must be specified when creating an empty array" }
            val types = values.mapTo(mutableSetOf()) { it.type }
            require(types.size == 1) { "All elements of an array must have the same type, but got $types" }
            types.single()
        })
    }

    public data class EnumEntry(val enumClass: Symbol.Classifier, val enumName: kotlin.String) : AnnotationArgument(AnnotationParameterType.Enum(enumClass)) {
        public constructor(enumEntry: Symbol.EnumEntry) : this(
            enumEntry.enumClass,
            enumEntry.name
        )
    }

    public data class KClass(val classSymbol: Symbol.Classifier) : AnnotationArgument(AnnotationParameterType.KClass)
    public data class Annotation<S : Symbol.Annotation<S, T>, T : Symbol.Annotation.Arguments<S, T>>(val annotationArguments: T) : AnnotationArgument(AnnotationParameterType.Annotation(annotationArguments.annotation))

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
        public fun kClass(value: Symbol.Classifier): KClass = KClass(value)

        public fun <T : AnnotationArgument> array(value: List<T>, elementType: AnnotationParameterType<*>): Array<T> = Array(value, elementType)
        public fun <T : AnnotationArgument> nonEmptyArray(value: List<T>): Array<T> = Array(value)
        public fun <T : AnnotationArgument> array(first: T, vararg values: T): Array<T> = Array(buildList {
            add(first)
            addAll(values)
        })

        public fun enum(value: Symbol.EnumEntry): EnumEntry = EnumEntry(value)
        public fun enum(value: Symbol.Classifier, name: kotlin.String): EnumEntry = EnumEntry(value, name)

        public fun <S : Symbol.Annotation<S, T>, T : Symbol.Annotation.Arguments<S, T>> annotation(value: T): Annotation<S, T> = Annotation(value)

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


public fun Symbol.EnumEntry.asAnnotationArgument(): EnumEntry = EnumEntry(enumClass, name)

