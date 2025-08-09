package dev.rnett.symbolexport.symbol.compiler.annotation.ir

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameter
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType
import dev.rnett.symbolexport.symbol.annotation.AnnotationWriter
import dev.rnett.symbolexport.symbol.annotation.BaseAnnotationWriter
import dev.rnett.symbolexport.symbol.compiler.asClassId
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrClassReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.name.Name

/**
 * Create an annotation [IrConstructorCall] from an annotation instance.
 */
@UnsafeDuringIrConstructionAPI
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> I.toIrAnnotation(
    context: IrPluginContext,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
    origin: IrStatementOrigin? = null,
    source: SourceElement? = null
): IrConstructorCall = IrAnnotationWriter(context, startOffset, endOffset, origin, source).write(this)

@UnsafeDuringIrConstructionAPI
private class IrAnnotationWriter(
    val context: IrPluginContext,
    val startOffset: Int = UNDEFINED_OFFSET,
    val endOffset: Int = UNDEFINED_OFFSET,
    val origin: IrStatementOrigin? = null,
    val source: SourceElement? = null
) : BaseAnnotationWriter<IrConstructorCall, IrExpression>() {
    override fun assembleAnnotation(
        annotation: Symbol.Annotation<*, *>,
        arguments: Map<AnnotationParameter<*>, IrExpression?>,
        isTopLevel: Boolean
    ): IrConstructorCall {
        val annotationClass = context.referenceClass(annotation.asClassId()) ?: error("No class found for $annotation")

        val primaryCtor = context.referenceConstructors(annotation.asClassId()).singleOrNull { it.owner.isPrimary } ?: error("No primary constructor found for $annotation")

        if (annotationClass.owner.typeParameters.isNotEmpty())
            error("Annotations with type parameters are not supported: $annotation")

        val call = IrConstructorCallImpl(
            startOffset,
            endOffset,
            annotationClass.defaultType,
            primaryCtor,
            0,
            0,
            origin,
            source ?: SourceElement.NO_SOURCE
        )

        arguments.forEach { (param, value) ->
            call.arguments[param.index] = value
        }

        return call
    }

    override fun writerForAnnotationArgument(annotation: Symbol.Annotation<*, *>): AnnotationWriter<IrExpression> {
        return this
    }

    override fun writeArrayArgument(
        elements: List<IrExpression>,
        elementType: AnnotationParameterType<*>
    ): IrExpression {
        return IrVarargImpl(
            startOffset,
            endOffset,
            context.irBuiltIns.arrayClass.typeWith(elementType.toIrType()),
            elementType.toIrType(),
            elements
        )
    }

    override fun writeEnumEntryArgument(enumClass: Symbol.Classifier, enumEntryName: String): IrExpression {
        val enumClass = context.referenceClass(enumClass.asClassId())?.owner ?: error("No class found for ${enumClass.asClassId()}")
        if (!enumClass.isEnumClass)
            error("Class ${enumClass.classId!!} is not an enum class")

        val enumEntrySymbol =
            enumClass.declarations.firstOrNull { it is IrEnumEntry && it.name == Name.guessByFirstCharacter(enumEntryName) } as IrEnumEntry? ?: error("No enum entry $enumEntryName found in ${enumClass.classId!!}")
        return IrGetEnumValueImpl(
            startOffset,
            endOffset,
            enumClass.defaultType,
            enumEntrySymbol.symbol
        )
    }

    override fun writeClassArgument(value: Symbol.Classifier): IrExpression {
        val symbol = context.referenceClass(value.asClassId()) ?: error("No class found for $value")
        val type = context.irBuiltIns.kClassClass.typeWith(symbol.starProjectedType)
        return IrClassReferenceImpl(startOffset, endOffset, type, symbol, symbol.starProjectedType)
    }

    override fun writePrimitiveArgument(value: AnnotationArgument.Primitive<*>): IrExpression {
        return value.value.toIrConst(
            when (value) {
                is AnnotationArgument.Boolean -> context.irBuiltIns.booleanType
                is AnnotationArgument.Byte -> context.irBuiltIns.byteType
                is AnnotationArgument.Char -> context.irBuiltIns.charType
                is AnnotationArgument.Double -> context.irBuiltIns.doubleType
                is AnnotationArgument.Float -> context.irBuiltIns.floatType
                is AnnotationArgument.Int -> context.irBuiltIns.intType
                is AnnotationArgument.Long -> context.irBuiltIns.longType
                is AnnotationArgument.Short -> context.irBuiltIns.shortType
                is AnnotationArgument.String -> context.irBuiltIns.stringType
            },
            startOffset,
            endOffset
        )
    }

    private fun AnnotationParameterType<*>.toIrType(): IrType = when (this) {
        is AnnotationParameterType.Annotation<*, *> -> context.referenceClass(annotationClass.asClassId())?.starProjectedType ?: throw IllegalArgumentException("No class found for $annotationClass")
        is AnnotationParameterType.Array<*, *> -> context.irBuiltIns.arrayClass.typeWith(elementType.toIrType())
        is AnnotationParameterType.Enum -> context.referenceClass(enumClass.asClassId())?.starProjectedType ?: throw IllegalArgumentException("No class found for $enumClass")
        AnnotationParameterType.KClass -> context.irBuiltIns.kClassClass.starProjectedType
        AnnotationParameterType.Boolean -> context.irBuiltIns.booleanType
        AnnotationParameterType.Byte -> context.irBuiltIns.byteType
        AnnotationParameterType.Char -> context.irBuiltIns.charType
        AnnotationParameterType.Double -> context.irBuiltIns.doubleType
        AnnotationParameterType.Float -> context.irBuiltIns.floatType
        AnnotationParameterType.Int -> context.irBuiltIns.intType
        AnnotationParameterType.Long -> context.irBuiltIns.longType
        AnnotationParameterType.Short -> context.irBuiltIns.shortType
        AnnotationParameterType.String -> context.irBuiltIns.stringType
    }
}