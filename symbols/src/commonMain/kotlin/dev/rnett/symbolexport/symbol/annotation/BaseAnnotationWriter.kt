package dev.rnett.symbolexport.symbol.annotation

import dev.rnett.symbolexport.symbol.Symbol

public class AnnotationParameterWriteException(public val parameter: AnnotationParameter<*>, public val actualValue: AnnotationArgument, cause: Throwable) :
    RuntimeException("Error writing argument for annotation parameter ${parameter.name} with expected type ${parameter.type}, the argument value was $actualValue", cause)

public interface AnnotationWriter<out R> {
    public fun write(arguments: Symbol.Annotation.Arguments<*, *>, isTopLevel: Boolean = true): R
}

public abstract class BaseAnnotationWriter<out R, E : Any> : AnnotationWriter<R> {

    protected abstract fun assembleAnnotation(annotation: Symbol.Annotation<*, *>, arguments: Map<AnnotationParameter<*>, E?>, isTopLevel: Boolean): R

    final override fun write(arguments: Symbol.Annotation.Arguments<*, *>, isTopLevel: Boolean): R {
        return assembleAnnotation(arguments.annotation, arguments.asMap.mapValues { (key, value) -> value?.let { writeArgument(key as AnnotationParameter<AnnotationParameterType<AnnotationArgument>>, it) } }, isTopLevel)
    }

    private fun <P : AnnotationParameterType<A>, A : AnnotationArgument> writeArgument(annotationParameter: AnnotationParameter<P>, argument: A): E {
        try {
            return writeArgument(argument)
        } catch (e: Throwable) {
            throw AnnotationParameterWriteException(annotationParameter, argument, e)
        }
    }

    private fun <P : AnnotationParameterType<A>, A : AnnotationArgument> writeArgument(argument: A): E {
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

    protected abstract fun writerForAnnotationArgument(annotation: Symbol.Annotation<*, *>): AnnotationWriter<E>

    protected abstract fun writeArrayArgument(elements: List<E>, elementType: AnnotationParameterType<*>): E

    protected abstract fun writeEnumEntryArgument(enumClass: Symbol.Classifier, enumEntryName: String): E

    protected abstract fun writeClassArgument(value: Symbol.Classifier): E

    protected abstract fun writePrimitiveArgument(value: AnnotationArgument.Primitive<*>): E
}