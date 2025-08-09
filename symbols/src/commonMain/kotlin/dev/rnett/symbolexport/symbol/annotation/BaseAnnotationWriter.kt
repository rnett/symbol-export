package dev.rnett.symbolexport.symbol.annotation

import dev.rnett.symbolexport.symbol.Symbol

/**
 * An error writing the argument of an annotation parameter.
 */
public class AnnotationParameterWriteException(public val parameter: AnnotationParameter<*>, public val argumentValue: AnnotationArgument, cause: Throwable) :
    RuntimeException("Error writing argument for annotation parameter ${parameter.name} with expected type ${parameter.type}, the argument value was $argumentValue", cause)

/**
 * An error assembling an annotation from its arguments.
 */
public class AnnotationAssemblyException(public val annotation: Symbol.Annotation<*, *>, cause: Throwable) :
    RuntimeException("Error assembling annotation $annotation from its arguments", cause)

/**
 * Writes an [Symbol.Annotation.Instance] to a raw annotation type [Annotation].
 *
 * Implementors likely want to use [BaseAnnotationWriter].
 */
public interface AnnotationWriter<out Annotation> {
    public fun write(instance: Symbol.Annotation.Instance<*, *>, isTopLevel: Boolean = true): Annotation
}

/**
 * A base implementation of [AnnotationWriter] that handles the argument writing and annotation assembly in two steps.
 * Mirrors [BaseAnnotationArgumentProducer].
 *
 * An annotation is written in two steps:
 *  * Getting the raw value (of [Argument] type) of each argument using one of the `write` methods.
 *  * Assembling the annotation from the raw arguments using [assembleAnnotation]
 */
public abstract class BaseAnnotationWriter<out Annotation, Argument : Any> : AnnotationWriter<Annotation> {

    /**
     * Assemble the annotation from the raw arguments.
     *
     * @param annotation The annotation type we are assembling.
     * @param arguments The raw arguments. All parameters are included in the map - the argument is null if it wasn't present in the annotation instance.
     * @param isTopLevel True if the annotation is being written onto a declaration, and false if it's being used as the argument of another annotation.
     */
    protected abstract fun assembleAnnotation(annotation: Symbol.Annotation<*, *>, arguments: Map<AnnotationParameter<*>, Argument?>, isTopLevel: Boolean): Annotation

    final override fun write(instance: Symbol.Annotation.Instance<*, *>, isTopLevel: Boolean): Annotation {
        val args = instance.arguments.mapValues { (key, value) -> value?.let { writeArgument(key as AnnotationParameter<AnnotationParameterType<AnnotationArgument>>, it) } }
        return try {
            assembleAnnotation(instance.annotation, args, isTopLevel)
        } catch (e: Throwable) {
            throw AnnotationAssemblyException(instance.annotation, e)
        }
    }

    private fun <P : AnnotationParameterType<A>, A : AnnotationArgument> writeArgument(annotationParameter: AnnotationParameter<P>, argument: A): Argument {
        try {
            return writeArgument(argument)
        } catch (e: Throwable) {
            throw AnnotationParameterWriteException(annotationParameter, argument, e)
        }
    }

    private fun <P : AnnotationParameterType<A>, A : AnnotationArgument> writeArgument(argument: A): Argument {
        @Suppress("UNCHECKED_CAST")
        return when (argument) {
            is AnnotationArgument.Annotation<*, *> -> writerForAnnotationArgument(argument.annotationArguments.annotation).write(argument.annotationArguments, false)
            is AnnotationArgument.Array<*> -> {
                val elements = argument.values.map { writeArgument(it) }
                writeArrayArgument(elements, argument.elementType)
            }

            is AnnotationArgument.EnumEntry -> writeEnumEntryArgument(argument.enumClass, argument.enumName)
            is AnnotationArgument.KClass -> writeClassArgument(argument.classSymbol)
            is AnnotationArgument.Primitive<*> -> writePrimitiveArgument(argument)
        }
    }

    /**
     * Get an annotation writer to use to write an instance of [annotation] when it is used as an argument.
     */
    protected abstract fun writerForAnnotationArgument(annotation: Symbol.Annotation<*, *>): AnnotationWriter<Argument>

    /**
     * Get the raw value of an array argument.
     */
    protected abstract fun writeArrayArgument(elements: List<Argument>, elementType: AnnotationParameterType<*>): Argument

    /**
     * Get the raw value of an enum entry argument.
     */
    protected abstract fun writeEnumEntryArgument(enumClass: Symbol.Classifier, enumEntryName: String): Argument

    /**
     * Get the raw value of a class argument.
     */
    protected abstract fun writeClassArgument(value: Symbol.Classifier): Argument

    /**
     * Get the raw value of a primitive argument.
     */
    protected abstract fun writePrimitiveArgument(value: AnnotationArgument.Primitive<*>): Argument
}