package dev.rnett.symbolexport.symbol.compiler.annotation.ir

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.annotation.BaseAnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.compiler.asClassId
import dev.rnett.symbolexport.symbol.compiler.asFqName
import dev.rnett.symbolexport.symbol.compiler.toClassifier
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstantArray
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getValueArgument
import org.jetbrains.kotlin.ir.util.isAnnotation
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass
import kotlin.reflect.safeCast


/**
 * Find any annotation instances of [annotation] type.
 */
@UnsafeDuringIrConstructionAPI
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> IrAnnotationContainer.findAnnotations(annotation: S): List<I> {
    return annotations.filter { it.isAnnotation(annotation.asFqName()) }.mapNotNull { it.readAnnotation(annotation) }
}

/**
 * Find the first annotation instance of [annotation] type.
 */
@UnsafeDuringIrConstructionAPI
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> IrAnnotationContainer.findAnnotation(annotation: S): I? {
    val ctorCall = getAnnotation(annotation.asFqName()) ?: return null
    return ctorCall.readAnnotation(annotation)
}

/**
 * Read an annotation instance from a [IrConstructorCall] for an annotation. Will return null if the annotation is not of type [annotation].
 */
@UnsafeDuringIrConstructionAPI
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> IrConstructorCall.readAnnotation(annotation: S): I? {
    if (annotation.asClassId() != this.symbol.owner.parentAsClass.classId)
        return null

    return annotation.produceInstance(IrAnnotationArgumentProducer(this))
}

@UnsafeDuringIrConstructionAPI
private class IrAnnotationArgumentProducer(val ctorCall: IrConstructorCall) : BaseAnnotationArgumentProducer<IrExpression>() {
    override fun renderForErrorReporting(raw: IrExpression): String {
        return raw.render()
    }

    override fun getRawValueForParameter(parameterName: String, parameterIndex: Int): IrExpression? {
        return ctorCall.getValueArgument(Name.guessByFirstCharacter(parameterName))
    }

    override fun extractAnnotationProducer(expression: IrExpression, expectedAnnotation: Symbol.Annotation<*, *>): AnnotationArgumentProducer {
        if (expression !is IrConstructorCall)
            throw IllegalArgumentException("Expected annotation")

        val annotationClass = expression.symbol.owner.parentAsClass.classId ?: throw IllegalArgumentException("Expected annotation with class")

        if (annotationClass != expectedAnnotation.asClassId())
            throw IllegalArgumentException("Expected annotation of type ${expectedAnnotation.asClassId()}, but got $annotationClass")

        return IrAnnotationArgumentProducer(expression)
    }

    override fun extractArrayArguments(expression: IrExpression): List<IrExpression> {
        if (expression is IrVararg)
            return expression.elements.map {
                when (it) {
                    is IrExpression -> it
                    is IrSpreadElement -> throw IllegalArgumentException("Spreads are not supported for array annotation arguments")
                    else -> error("Unknown vararg element $it")
                }
            }

        if (expression is IrConstantArray)
            return expression.elements

        throw IllegalArgumentException("Expected array literal")
    }

    override fun extractClass(expression: IrExpression): Symbol.Classifier {
        if (expression !is IrClassReference)
            throw IllegalArgumentException("Expected class literal")
        val owner = expression.symbol.owner as? IrClass ?: throw IllegalArgumentException("Expected class literal")
        return owner.classId?.toClassifier() ?: throw IllegalArgumentException("Class for literal not found")
    }

    override fun extractEnumInfo(expression: IrExpression): AnnotationArgument.EnumEntry {
        if (expression !is IrGetEnumValue)
            throw IllegalArgumentException("Expected enum literal")

        return AnnotationArgument.EnumEntry(
            expression.symbol.owner.parentAsClass.classId!!.toClassifier(),
            expression.symbol.owner.name.asString()
        )
    }

    override fun <T : Any> extractPrimitiveValue(expression: IrExpression, type: KClass<T>): T {
        if (expression !is IrConst)
            throw IllegalArgumentException("Expected ${type.simpleName} literal")
        return type.safeCast(expression.value) ?: throw IllegalArgumentException("Expected ${type.simpleName} literal")
    }
}