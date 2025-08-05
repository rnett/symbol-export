package dev.rnett.symbolexport.symbol.annotation

import dev.rnett.symbolexport.symbol.Symbol

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

    public data class Array<T : AnnotationArgument>(val values: List<T>, val elementType: AnnotationParameterType<*>) : AnnotationArgument(AnnotationParameterType.Array(elementType)), List<T> by values
    public data class EnumEntry(val enumClass: Symbol.Classifier, val enumName: kotlin.String) : AnnotationArgument(AnnotationParameterType.Enum(enumClass))
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

}

public fun Symbol.EnumEntry.asAnnotationArgument(): AnnotationArgument.EnumEntry = AnnotationArgument.EnumEntry(enumClass, name)