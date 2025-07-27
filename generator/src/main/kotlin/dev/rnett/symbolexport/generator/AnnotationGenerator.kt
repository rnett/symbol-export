package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.generator.InternalNameGenerationHandler.getFieldName
import dev.rnett.symbolexport.generator.InternalNameGenerationHandler.nameSegmentsOf
import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import java.util.*

internal object AnnotationGenerator {
    fun annotationClassName(name: InternalName.Annotation): String = getFieldName(name) + "_Spec"
    fun annotationClassName(name: InternalName.Classifier): String = getFieldName(name) + "_Spec"

    fun generateClass(name: InternalName.Annotation): String {
        val className = annotationClassName(name)

        return buildString {
            appendLine("class $className private constructor() : Symbol.Annotation<$className, Arguments>(")
            appendLine("    packageName = ${nameSegmentsOf(name.packageName)},")
            appendLine("    classNames = ${nameSegmentsOf(name.classNames)},")
            appendLine(") {")

            appendIndentedLine {
                appendLine()
                name.parameters.forEach {
                    appendLine("public val ${it.key}: AnnotationParameter by lazy { AnnotationParameter(\"${it.key}\", ${annotationTypeConstructor(it.value)}) }")
                    appendLine()
                }

                appendLine("public inner class Arguments private constructor(producer: AnnotationArgumentProducer) : Symbol.Annotation.Arguments<$className, Arguments>() {")

                appendIndentedLine {
                    appendLine("override val annotation: $className get() = this@$className")
                    if (name.parameters.isNotEmpty())
                        appendLine()

                    name.parameters.forEach {
                        appendLine("public val ${it.key}: ${annotationValueType(it.value)} = producer.getArgument(this@${className}.${it.key})")
                    }
                }

                appendLine("}")

                appendLine("override fun produceArguments(producer: AnnotationArgumentProducer): Arguments = Arguments(producer)")
            }

            appendLine("}")
        }
    }

    fun annotationTypeConstructor(type: AnnotationParameterType): String {
        return when (type) {
            is AnnotationParameterType.Annotation -> "AnnotationParameterType.Annotation<${annotationClassName(type.annotationClass)}>(annotationClass = ${getFieldName(type.annotationClass)})"
            is AnnotationParameterType.Array -> "AnnotationParameterType.Array(elementType = ${annotationTypeConstructor(type.elementType)})"
            is AnnotationParameterType.Enum -> "AnnotationParameterType.Enum(enumClass = ${getFieldName(type.enumClass)})"
            AnnotationParameterType.KClass -> "AnnotationParameterType.KClass"
            is AnnotationParameterType.Primitive -> "AnnotationParameterType.${type.typeName}"
        }
    }

    fun annotationValueType(type: AnnotationParameterType): String {
        return when (type) {
            is AnnotationParameterType.Annotation -> "AnnotationArgument.Annotation<${annotationClassName(type.annotationClass)}.Arguments>"
            is AnnotationParameterType.Array -> "AnnotationArgument.Array<${annotationValueType(type.elementType)}>"
            is AnnotationParameterType.Enum -> "AnnotationArgument.EnumEntry"
            AnnotationParameterType.KClass -> "AnnotationArgument.KClass"
            is AnnotationParameterType.Primitive -> "AnnotationArgument.${type.typeName}"
        }
    }

    private val AnnotationParameterType.Primitive.typeName: String get() = name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}