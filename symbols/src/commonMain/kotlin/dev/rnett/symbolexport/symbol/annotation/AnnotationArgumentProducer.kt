package dev.rnett.symbolexport.symbol.annotation

import dev.rnett.symbolexport.symbol.Symbol
import kotlin.reflect.KClass

public interface AnnotationArgumentProducer {
    public fun <T : AnnotationArgument, P : AnnotationParameterType<T>> getArgument(parameter: AnnotationParameter<P>): T?
}

public class AnnotationParameterReadException(public val parameter: AnnotationParameter<*>, public val actualValue: String, cause: Throwable) :
    RuntimeException("Error reading annotation parameter ${parameter.name} with expected type ${parameter.type}, actual value was $actualValue", cause)

public abstract class BaseAnnotationArgumentProducer<E> : AnnotationArgumentProducer {
    override fun <T : AnnotationArgument, P : AnnotationParameterType<T>> getArgument(parameter: AnnotationParameter<P>): T? {
        val raw = getRawValueForParameter(parameter.name) ?: return null
        @Suppress("UNCHECKED_CAST")
        return try {
            extractAnnotationArgument(raw, parameter.type)
        } catch (e: Throwable) {
            throw AnnotationParameterReadException(parameter, renderForErrorReporting(raw), e)
        }
    }

    protected abstract fun renderForErrorReporting(raw: E): String

    protected abstract fun getRawValueForParameter(parameterName: String): E?

    private fun <T : AnnotationArgument, P : AnnotationParameterType<T>> extractAnnotationArgument(expression: E, type: P): T {
        val result = when (type) {
            is AnnotationParameterType.Annotation<*, *> -> AnnotationArgument.Annotation(type.annotationClass.produceArguments(extractAnnotationProducer(expression, type.annotationClass)))
            is AnnotationParameterType.Array<*, *> -> AnnotationArgument.Array(extractArrayArguments(expression).map { extractAnnotationArgument(it, type.elementType) }, type.elementType as AnnotationParameterType<AnnotationArgument>)
            is AnnotationParameterType.Enum -> extractEnumInfo(expression)
            AnnotationParameterType.KClass -> AnnotationArgument.KClass(extractClass(expression))
            is AnnotationParameterType.Primitive<*, *> -> extractPrimitiveArgument(expression, type)
        }

        if (result.type != type)
            throw IllegalArgumentException("Expected argument with type $type, got ${result.type} for $result")

        return result as T
    }

    protected abstract fun extractAnnotationProducer(expression: E, expectedAnnotation: Symbol.Annotation<*, *>): AnnotationArgumentProducer

    protected abstract fun extractArrayArguments(expression: E): List<E>

    protected abstract fun extractEnumInfo(expression: E): AnnotationArgument.EnumEntry

    protected abstract fun extractClass(expression: E): Symbol.Classifier

    protected open fun <T : Any, A : AnnotationArgument.Primitive<T>> extractPrimitiveArgument(expression: E, type: AnnotationParameterType.Primitive<T, A>): A {
        val value = extractPrimitiveValue(expression, type.kClass)

        if (!type.kClass.isInstance(value)) {
            throw IllegalArgumentException("Expected value of type ${type.kClass.simpleName}, got ${value::class} for $value")
        }

        return type.createArgument(value)
    }

    protected abstract fun <T : Any> extractPrimitiveValue(expression: E, type: KClass<T>): T
}

public abstract class BasePrimitiveSpecificAnnotationArgumentProducer<E> : BaseAnnotationArgumentProducer<E>() {

    final override fun <T : Any, A : AnnotationArgument.Primitive<T>> extractPrimitiveArgument(expression: E, type: AnnotationParameterType.Primitive<T, A>): A {
        return when (type) {
            AnnotationParameterType.Boolean -> extractBoolean(expression).let { AnnotationArgument.Boolean(it) as A }
            AnnotationParameterType.Byte -> extractByte(expression).let { AnnotationArgument.Byte(it) as A }
            AnnotationParameterType.Char -> extractChar(expression).let { AnnotationArgument.Char(it) as A }
            AnnotationParameterType.Double -> extractDouble(expression).let { AnnotationArgument.Double(it) as A }
            AnnotationParameterType.Float -> extractFloat(expression).let { AnnotationArgument.Float(it) as A }
            AnnotationParameterType.Int -> extractInt(expression).let { AnnotationArgument.Int(it) as A }
            AnnotationParameterType.Long -> extractLong(expression).let { AnnotationArgument.Long(it) as A }
            AnnotationParameterType.Short -> extractShort(expression).let { AnnotationArgument.Short(it) as A }
            AnnotationParameterType.String -> extractString(expression).let { AnnotationArgument.String(it) as A }
        }
    }

    override fun <T : Any> extractPrimitiveValue(expression: E, type: KClass<T>): T {
        throw UnsupportedOperationException("Should never be called")
    }

    protected abstract fun extractBoolean(expression: E): Boolean
    protected abstract fun extractByte(expression: E): Byte
    protected abstract fun extractChar(expression: E): Char
    protected abstract fun extractDouble(expression: E): Double
    protected abstract fun extractFloat(expression: E): Float
    protected abstract fun extractInt(expression: E): Int
    protected abstract fun extractLong(expression: E): Long
    protected abstract fun extractShort(expression: E): Short
    protected abstract fun extractString(expression: E): String

}