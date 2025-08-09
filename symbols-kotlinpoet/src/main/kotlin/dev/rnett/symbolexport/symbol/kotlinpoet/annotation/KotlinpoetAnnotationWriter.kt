package dev.rnett.symbolexport.symbol.kotlinpoet.annotation

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameter
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType
import dev.rnett.symbolexport.symbol.annotation.AnnotationWriter
import dev.rnett.symbolexport.symbol.annotation.BaseAnnotationWriter
import dev.rnett.symbolexport.symbol.kotlinpoet.asClassName

/**
 * Create an [AnnotationSpec] from an annotation instance.
 */
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> I.toAnnotationSpec(
    useSiteTarget: AnnotationSpec.UseSiteTarget? = null
): AnnotationSpec = KotlinpoetAnnotationWriter(useSiteTarget).write(this)

private class KotlinpoetAnnotationWriter(val useSiteTarget: AnnotationSpec.UseSiteTarget?) : BaseAnnotationWriter<AnnotationSpec, CodeBlock>() {
    override fun assembleAnnotation(
        annotation: Symbol.Annotation<*, *>,
        arguments: Map<AnnotationParameter<*>, CodeBlock?>,
        isTopLevel: Boolean
    ): AnnotationSpec {
        return AnnotationSpec.builder(annotation.asClassName()).apply {
            if (isTopLevel)
                useSiteTarget(useSiteTarget)

            arguments.forEach { (key, value) ->
                if (value != null)
                    addMember(CodeBlock.of("%L = %L", key.name, value))
            }
        }.build()
    }

    override fun writerForAnnotationArgument(annotation: Symbol.Annotation<*, *>): AnnotationWriter<CodeBlock> {
        return object : AnnotationWriter<CodeBlock> {
            override fun write(instance: Symbol.Annotation.Instance<*, *>, isTopLevel: Boolean): CodeBlock {
                val a = this@KotlinpoetAnnotationWriter.write(instance, isTopLevel)
                return CodeBlock.of("%L", a)
            }

        }
    }

    override fun writeArrayArgument(elements: List<CodeBlock>, elementType: AnnotationParameterType<*>): CodeBlock {
        return CodeBlock.builder().apply {
            add("[")
            elements.forEach { add("%L,", it) }
            add("]")
        }.build()
    }

    override fun writeEnumEntryArgument(enumClass: Symbol.Classifier, enumEntryName: String): CodeBlock {
        return CodeBlock.of("%T.%L", enumClass.asClassName(), enumEntryName)
    }

    override fun writeClassArgument(value: Symbol.Classifier): CodeBlock {
        return CodeBlock.of("%T::class", value.asClassName())
    }

    override fun writePrimitiveArgument(value: AnnotationArgument.Primitive<*>): CodeBlock {
        if (value is AnnotationArgument.String)
            return CodeBlock.of("%S", value.value)
        return CodeBlock.of("%L", value.value)
    }
}