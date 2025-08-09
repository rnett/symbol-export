package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.generator.InternalNameGenerationHandler.getFieldName
import dev.rnett.symbolexport.generator.InternalNameGenerationHandler.nameSegmentsOf
import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import java.util.*

internal object AnnotationGenerator {
    fun annotationClassName(name: InternalName.Annotation): String = getFieldName(name) + "_Spec"
    fun annotationClassName(name: InternalName.Classifier): String = getFieldName(name) + "_Spec"

    fun generateClass(name: InternalName.Annotation, referencable: Set<InternalName>): String {
        val className = annotationClassName(name)

        return buildString {
            appendLine("class $className() : Symbol.Annotation<$className, ${className}.Instance>(")
            appendLine("    packageName = ${nameSegmentsOf(name.packageName)},")
            appendLine("    classNames = ${nameSegmentsOf(name.classNames)},")
            appendLine(") {")

            appendIndentedLine {
                appendLine()
                appendParameterProperties(name, referencable)

                appendInstanceClass(name, referencable)
                appendLine()

                appendProduceInstance(name, referencable)

                appendLine()

                appendCreateInstance(name, referencable)

                appendLine()

                appendInvoke(name)

                appendLine()

                appendParametersListProperty(name)
            }

            appendLine("}")
        }
    }

    // Generates top-level AnnotationParameter properties for each annotation parameter
    private fun StringBuilder.appendParameterProperties(name: InternalName.Annotation, referencable: Set<InternalName>) {
        name.parameters.forEach {
            appendLine("public val ${it.name}: AnnotationParameter<${annotationParameterType(it.type)}> by lazy {")
            appendIndentedLine { append("AnnotationParameter(\"${it.name}\", ${it.index}, ${annotationParameterTypeConstructor(it.type, referencable)})") }
            appendLine("}")
            appendLine()
        }
    }

    // Generates the inner Instance class, including annotation getter and asMap property
    private fun StringBuilder.appendInstanceClass(name: InternalName.Annotation, referencable: Set<InternalName>) {
        val className = annotationClassName(name)
        append("public inner class Instance(")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                name.parameters.forEach {
                    appendLine("public val ${it.name}: ${annotationValueType(it.type)}?,")
                }
            }
        }
        appendLine(") : Symbol.Annotation.Instance<$className, Instance>() {")

        appendIndentedLine {
            appendLine("override val annotation: $className get() = this@$className")
            appendLine()
            appendInstanceArgumentsMap(name)
        }

        appendLine("}")
    }

    // Generates the arguments property inside the Instance class
    private fun StringBuilder.appendInstanceArgumentsMap(name: InternalName.Annotation) {
        val className = annotationClassName(name)
        append("override val arguments: Map<AnnotationParameter<*>, AnnotationArgument?> by lazy {")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                appendLine("buildMap {")
                appendIndentedLine {
                    name.parameters.forEach {
                        appendLine("put(this@${className}.${it.name}, ${it.name})")
                    }
                }
                appendLine("}")
            }
            appendLine("}")
        } else {
            appendLine(" emptyMap() }")
        }
    }

    // Generates the produceInstance method
    private fun StringBuilder.appendProduceInstance(name: InternalName.Annotation, referencable: Set<InternalName>) {
        val className = annotationClassName(name)
        append("override fun produceInstance(producer: AnnotationArgumentProducer): ${className}.Instance = Instance(")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                name.parameters.forEach {
                    appendLine("producer.getArgument(this@${className}.${it.name}),")
                }
            }
        }
        appendLine(")")
    }

    // Generates the createInstance method
    private fun StringBuilder.appendCreateInstance(name: InternalName.Annotation, referencable: Set<InternalName>) {
        val className = annotationClassName(name)
        append("public fun createInstance(")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                name.parameters.forEach {
                    appendLine("${it.name}: ${annotationCreationValueType(it.type)}?,")
                }
            }
        }
        append("): ${className}.Instance = Instance(")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                name.parameters.forEach {
                    appendLine("${annotationCreationConvertedValue(it.name, it.type, referencable)},")
                }
            }
        }
        appendLine(")")
    }

    // Generates the operator invoke method
    private fun StringBuilder.appendInvoke(name: InternalName.Annotation) {
        val className = annotationClassName(name)
        append("public operator fun invoke(")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                name.parameters.forEach {
                    appendLine("${it.name}: ${annotationCreationValueType(it.type)}?,")
                }
            }
        }
        append("): ${className}.Instance = createInstance(")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                name.parameters.forEach {
                    appendLine("${it.name},")
                }
            }
        }
        appendLine(")")
    }

    // Generates the parameters list property
    private fun StringBuilder.appendParametersListProperty(name: InternalName.Annotation) {
        append("override val parameters: List<AnnotationParameter<*>> by lazy {")
        if (name.parameters.isNotEmpty()) {
            appendLine()
            appendIndentedLine {
                appendLine("listOf(")
                appendIndentedLine {
                    name.parameters.sortedBy { it.index }.forEach {
                        appendLine("${it.name},")
                    }
                }
                appendLine(")")
            }
            appendLine("}")
        } else {
            appendLine(" emptyList() }")
        }
    }

    fun annotationCreationConvertedValue(param: String, type: AnnotationParameterType, referencable: Set<InternalName>, nullable: Boolean = true): String {
        val paramName = if (nullable) "it" else param
        val body: String = when (type) {
            is AnnotationParameterType.Primitive -> "AnnotationArgument.${type.typeName}($paramName)"
            is AnnotationParameterType.Array -> "AnnotationArgument.Array(it.map { ${annotationCreationConvertedValue("it", type.elementType, referencable, false)} }, ${annotationParameterTypeConstructor(type.elementType, referencable)})"
            is AnnotationParameterType.Annotation -> "AnnotationArgument.Annotation($paramName)"

            else -> return param
        }

        if (!nullable)
            return body

        return "$param?.let { $body }"
    }

    fun annotationCreationValueType(type: AnnotationParameterType): String = when (type) {
        is AnnotationParameterType.Primitive -> type.typeName
        is AnnotationParameterType.Array -> "List<${annotationCreationValueType(type.elementType)}>"
        is AnnotationParameterType.Annotation -> annotationClassName(type.annotationClass) + ".Instance"
        else -> annotationValueType(type)
    }

    fun annotationParameterTypeConstructor(type: AnnotationParameterType, referencable: Set<InternalName>): String {
        return when (type) {
            is AnnotationParameterType.Annotation -> "AnnotationParameterType.Annotation(annotationClass = ${getFieldName(type.annotationClass)})"
            is AnnotationParameterType.Array -> "AnnotationParameterType.Array(elementType = ${annotationParameterTypeConstructor(type.elementType, referencable)})"
            is AnnotationParameterType.Enum -> "AnnotationParameterType.Enum(enumClass = ${InternalNameGenerationHandler.generateConstructorOrReference(type.enumClass, referencable)})"
            AnnotationParameterType.KClass -> "AnnotationParameterType.KClass"
            is AnnotationParameterType.Primitive -> "AnnotationParameterType.${type.typeName}"
        }
    }

    fun annotationParameterType(type: AnnotationParameterType): String {
        return when (type) {
            is AnnotationParameterType.Annotation -> "AnnotationParameterType.Annotation<${annotationClassName(type.annotationClass)}, ${annotationClassName(type.annotationClass)}.Instance>"
            is AnnotationParameterType.Array -> "AnnotationParameterType.Array<${annotationParameterType(type.elementType)}, ${annotationValueType(type.elementType)}>"
            is AnnotationParameterType.Enum -> "AnnotationParameterType.Enum"
            AnnotationParameterType.KClass -> "AnnotationParameterType.KClass"
            is AnnotationParameterType.Primitive -> "AnnotationParameterType.${type.typeName}"
        }
    }

    fun annotationValueType(type: AnnotationParameterType): String {
        return when (type) {
            is AnnotationParameterType.Annotation -> "AnnotationArgument.Annotation<${annotationClassName(type.annotationClass)}, ${annotationClassName(type.annotationClass)}.Instance>"
            is AnnotationParameterType.Array -> "AnnotationArgument.Array<${annotationValueType(type.elementType)}>"
            is AnnotationParameterType.Enum -> "AnnotationArgument.EnumEntry"
            AnnotationParameterType.KClass -> "AnnotationArgument.KClass"
            is AnnotationParameterType.Primitive -> "AnnotationArgument.${type.typeName}"
        }
    }

    private val AnnotationParameterType.Primitive.typeName: String get() = name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}