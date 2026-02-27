package dev.rnett.symbolexport.symbol.annotation

import dev.rnett.symbolexport.symbol.Symbol
import kotlin.reflect.KClass

/**
 * Produces [AnnotationArgument]s for given parameters, or null if the parameter is not present.
 * Typically based on some underlying "raw" instance, i.e. `IrAnnotation`.
 *
 * Used to create [Symbol.Annotation.Instance]s for [Symbol.Annotation]s.
 *
 * If implementing your own, you probably should extend [BaseAnnotationArgumentProducer] or [BasePrimitiveSpecificAnnotationArgumentProducer] rather than this directly.
 */
public interface AnnotationArgumentProducer {
    /**
     * Returns the argument for the corresponding parameter, or null if the parameter is not present.
     * The returned argument's [AnnotationArgument.type] must match the parameter's [AnnotationParameter.type].
     *
     * @throws AnnotationArgumentExtractionException if there is an error converting the raw value of an argument to an [AnnotationArgument].
     * @throws AnnotationGettingArgumentsException if there is an error getting the raw value of an argument.
     */
    public fun <A : AnnotationArgument, P : AnnotationParameterType<A>> getArgument(parameter: AnnotationParameter<P>): A?

    @Suppress("UNCHECKED_CAST")
    public fun produceMap(parameters: List<AnnotationParameter<*>>): Map<AnnotationParameter<*>, AnnotationArgument> = buildMap {
        parameters.forEach { parameter ->
            val argument = getArgument<AnnotationArgument, _>(parameter as AnnotationParameter<Nothing>)
            if (argument != null) {
                put(parameter, argument)
            }
        }
    }
}

/**
 * An error getting the raw value of an argument.
 */
public class AnnotationGettingArgumentsException(public val parameter: AnnotationParameter<*>, cause: Throwable) :
    IllegalArgumentException("Error getting raw argument for annotation parameter ${parameter.name}", cause)

/**
 * An error extracting an [AnnotationArgument] for a parameter from the raw value.
 */
public class AnnotationArgumentExtractionException(public val parameter: AnnotationParameter<*>, public val rawValue: String, cause: Throwable) :
    IllegalArgumentException("Error extracting AnnotationArgument for annotation parameter ${parameter.name} with expected type ${parameter.type}, and raw value $rawValue", cause)

/**
 * A base class for implementing [AnnotationArgumentProducer] handles parameter types and wrapping for you.
 * You only have to implement the typed extractor methods.
 * If you want individual methods for each primitive type, use [BasePrimitiveSpecificAnnotationArgumentProducer] instead.
 *
 * Processing is done in two steps:
 *  * Getting the raw value (of type [Raw]) for the parameter using [getRawValueForParameter]
 *  * Extracting an [AnnotationArgument] from the raw [Raw] value using the appropriate `extract` method based on the parameter type
 *
 */
public abstract class BaseAnnotationArgumentProducer<Raw> : AnnotationArgumentProducer {
    override fun <A : AnnotationArgument, P : AnnotationParameterType<A>> getArgument(parameter: AnnotationParameter<P>): A? {
        val raw = try {
            getRawValueForParameter(parameter.name, parameter.index) ?: return null
        } catch (e: Throwable) {
            throw AnnotationGettingArgumentsException(parameter, e)
        }

        @Suppress("UNCHECKED_CAST")
        return try {
            extractAnnotationArgument(raw, parameter.type)
        } catch (e: Throwable) {
            throw AnnotationArgumentExtractionException(parameter, renderForErrorReporting(raw), e)
        }
    }

    /**
     * Renders a raw value for use in [AnnotationArgumentExtractionException].
     */
    protected abstract fun renderForErrorReporting(raw: Raw): String

    /**
     * Get the raw value of the annotation argument corresponding to the parameter with the given name and index.
     * Returns `null` if there is no such argument.
     */
    protected abstract fun getRawValueForParameter(parameterName: String, parameterIndex: Int): Raw?

    @Suppress("UNCHECKED_CAST")
    private fun <T : AnnotationArgument, P : AnnotationParameterType<T>> extractAnnotationArgument(expression: Raw, type: P): T {
        val result = when (type) {
            is AnnotationParameterType.Annotation<*, *> -> AnnotationArgument.Annotation(type.annotationClass.produceInstance(extractAnnotationProducer(expression, type.annotationClass)))
            is AnnotationParameterType.Array<*, *> -> AnnotationArgument.Array(extractArrayArguments(expression).map { extractAnnotationArgument(it, type.elementType) }, type.elementType as AnnotationParameterType<AnnotationArgument>)
            is AnnotationParameterType.Enum -> extractEnumInfo(expression)
            AnnotationParameterType.KClass -> AnnotationArgument.KClass(extractClass(expression))
            is AnnotationParameterType.Primitive<*, *> -> extractPrimitiveArgument(expression, type)
        }

        return result.asTypeOrNull(type) ?: throw IllegalArgumentException("Expected argument with type $type, got ${result.type} for $result")
    }

    /**
     * Convert a raw expression to an annotation producer that will be used to create a [Symbol.Annotation.Instance] for an annotation argument.
     */
    protected abstract fun extractAnnotationProducer(expression: Raw, expectedAnnotation: Symbol.Annotation<*, *>): AnnotationArgumentProducer

    /**
     * Convert a raw expression to a list of arguments for an array argument.
     */
    protected abstract fun extractArrayArguments(expression: Raw): List<Raw>

    /**
     * Convert a raw expression to an enum argument.
     */
    protected abstract fun extractEnumInfo(expression: Raw): AnnotationArgument.EnumEntry

    /**
     * Convert a raw expression to a class argument.
     */
    protected abstract fun extractClass(expression: Raw): Symbol.Classifier

    /**
     * Convert a raw expression to a primitive argument.
     */
    protected open fun <T : Any, A : AnnotationArgument.Primitive<T>> extractPrimitiveArgument(expression: Raw, type: AnnotationParameterType.Primitive<T, A>): A {
        val value = extractPrimitiveValue(expression, type.kClass)

        if (!type.kClass.isInstance(value)) {
            throw IllegalArgumentException("Expected value of type ${type.kClass.simpleName}, got ${value::class} for $value")
        }

        return type.createArgument(value)
    }

    /**
     * Convert a raw expression to a primitive value. Will be wrapped in a primitive argument based on the expected type.
     */
    protected abstract fun <T : Any> extractPrimitiveValue(expression: Raw, type: KClass<T>): T
}

/**
 * A variant of [BaseAnnotationArgumentProducer] that has a method for each primitive type.
 */
public abstract class BasePrimitiveSpecificAnnotationArgumentProducer<Raw> : BaseAnnotationArgumentProducer<Raw>() {

    final override fun <T : Any, A : AnnotationArgument.Primitive<T>> extractPrimitiveArgument(expression: Raw, type: AnnotationParameterType.Primitive<T, A>): A {
        return when (type) {
            AnnotationParameterType.Boolean -> extractBoolean(expression).let { AnnotationArgument.Boolean(it) }
            AnnotationParameterType.Byte -> extractByte(expression).let { AnnotationArgument.Byte(it) }
            AnnotationParameterType.Char -> extractChar(expression).let { AnnotationArgument.Char(it) }
            AnnotationParameterType.Double -> extractDouble(expression).let { AnnotationArgument.Double(it) }
            AnnotationParameterType.Float -> extractFloat(expression).let { AnnotationArgument.Float(it) }
            AnnotationParameterType.Int -> extractInt(expression).let { AnnotationArgument.Int(it) }
            AnnotationParameterType.Long -> extractLong(expression).let { AnnotationArgument.Long(it) }
            AnnotationParameterType.Short -> extractShort(expression).let { AnnotationArgument.Short(it) }
            AnnotationParameterType.String -> extractString(expression).let { AnnotationArgument.String(it) }
        }.asType(type)
    }

    override fun <T : Any> extractPrimitiveValue(expression: Raw, type: KClass<T>): T {
        throw UnsupportedOperationException("Should never be called")
    }

    protected abstract fun extractBoolean(expression: Raw): Boolean
    protected abstract fun extractByte(expression: Raw): Byte
    protected abstract fun extractChar(expression: Raw): Char
    protected abstract fun extractDouble(expression: Raw): Double
    protected abstract fun extractFloat(expression: Raw): Float
    protected abstract fun extractInt(expression: Raw): Int
    protected abstract fun extractLong(expression: Raw): Long
    protected abstract fun extractShort(expression: Raw): Short
    protected abstract fun extractString(expression: Raw): String

}