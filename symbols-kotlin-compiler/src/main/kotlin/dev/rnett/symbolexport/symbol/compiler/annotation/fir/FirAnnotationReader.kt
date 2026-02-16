package dev.rnett.symbolexport.symbol.compiler.annotation.fir

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.annotation.BaseAnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.compiler.asClassId
import dev.rnett.symbolexport.symbol.compiler.toClassifier
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.evaluateAs
import org.jetbrains.kotlin.fir.declarations.extractEnumValueArgumentInfo
import org.jetbrains.kotlin.fir.declarations.findArgumentByName
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getAnnotationsByClassId
import org.jetbrains.kotlin.fir.declarations.getTargetType
import org.jetbrains.kotlin.fir.declarations.resolved
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirArrayLiteral
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.resolvedArgumentMapping
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.expressions.unwrapExpression
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

/**
 * Find any annotation instances of [annotation] type.
 */
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> FirAnnotationContainer.findAnnotations(annotation: S, session: FirSession): List<I> {
    return getAnnotationsByClassId(annotation.asClassId(), session).mapNotNull { it.readAnnotation(annotation, session) }
}

/**
 * Find any annotation instances of [annotation] type.
 */
@OptIn(SymbolInternals::class)
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> FirBasedSymbol<*>.findAnnotations(annotation: S, session: FirSession): List<I> {
    return fir.findAnnotations(annotation, session)
}

/**
 * Find the first annotation instance of [annotation] type.
 */
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> FirAnnotationContainer.findAnnotation(annotation: S, session: FirSession): I? {
    val value = getAnnotationByClassId(annotation.asClassId(), session) ?: return null
    return value.readAnnotation(annotation, session)
}

/**
 * Find the first annotation instance of [annotation] type.
 */
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> FirBasedSymbol<*>.findAnnotation(annotation: S, session: FirSession): I? {
    val value = getAnnotationByClassId(annotation.asClassId(), session) ?: return null
    return value.readAnnotation(annotation, session)
}

/**
 * Read an annotation instance from a [FirAnnotation]. Will return null if the annotation is not of type [annotation].
 */
public fun <S : Symbol.Annotation<S, I>, I : Symbol.Annotation.Instance<S, I>> FirAnnotation.readAnnotation(annotation: S, session: FirSession): I? {
    if (annotation.asClassId() != this.toAnnotationClassId(session))
        return null
    return annotation.produceInstance(FirAnnotationArgumentProvider(this, session))
}

private abstract class BaseFirAnnotationArgumentProvider(val session: FirSession) : BaseAnnotationArgumentProducer<FirExpression>() {
    override fun renderForErrorReporting(raw: FirExpression): String {
        return raw.render()
    }

    override fun extractAnnotationProducer(expression: FirExpression, expectedAnnotation: Symbol.Annotation<*, *>): AnnotationArgumentProducer {
        val annotationExpr = expression.evaluateAs<FirAnnotation>(session)
        if (annotationExpr != null) {
            val actualClassId = annotationExpr.toAnnotationClassId(session)
            if (actualClassId != expectedAnnotation.asClassId()) {
                throw IllegalArgumentException("Expected annotation of type ${expectedAnnotation.asClassId()}, but got $actualClassId")
            }
            return FirAnnotationArgumentProvider(annotationExpr, session)
        }

        val callExpr = expression.evaluateAs<FirFunctionCall>(session) ?: throw IllegalArgumentException("Expected annotation")
        val target = callExpr.toResolvedCallableSymbol(session) ?: throw IllegalArgumentException("Expected annotation constructor call, but target symbol could not be resolved")
        val annotationClass = target.getContainingClassSymbol() ?: throw IllegalArgumentException("Expected annotation constructor call, but the call's target did not have a containing class")
        if (annotationClass.classId != expectedAnnotation.asClassId())
            throw IllegalArgumentException("Expected annotation of type ${expectedAnnotation.asClassId()}, but got ${annotationClass.classId}")
        return FirAnnotationCallArgumentProvider(callExpr, session)
    }

    override fun extractArrayArguments(expression: FirExpression): List<FirExpression> {
        val arrayLiteral = expression.evaluateAs<FirArrayLiteral>(session) ?: throw IllegalArgumentException("Expected array literal")
        return arrayLiteral.arguments
    }

    override fun extractClass(expression: FirExpression): Symbol.Classifier {
        val getClassCall = expression.evaluateAs<FirGetClassCall>(session) ?: throw IllegalArgumentException("Expected class literal")
        val classId = getClassCall.getTargetType()?.classId ?: throw IllegalArgumentException("Expected class literal")
        return classId.toClassifier()
    }

    override fun extractEnumInfo(expression: FirExpression): AnnotationArgument.EnumEntry {
        val enum = expression.extractEnumValueArgumentInfo() ?: throw IllegalArgumentException("Expected enum literal")
        return AnnotationArgument.EnumEntry(
            enum.enumClassId?.toClassifier() ?: throw IllegalArgumentException("Enum class for literal not found"),
            enum.enumEntryName.asString()
        )
    }

    override fun <T : Any> extractPrimitiveValue(expression: FirExpression, type: KClass<T>): T {
        val literal = expression.evaluateAs<FirLiteralExpression>(session) ?: throw IllegalArgumentException("Expected ${type.simpleName} literal")

        var value = literal.value

        if (type == Int::class && value is Number) {
            value = value.toInt()
        } else if (type == Long::class && value is Number) {
            value = value.toLong()
        } else if (type == Short::class && value is Number) {
            value = value.toShort()
        } else if (type == Byte::class && value is Number) {
            value = value.toByte()
        } else if (type == Char::class && value is Number) {
            value = value.toInt().toChar()
        } else if (type == Float::class && value is Number) {
            value = value.toFloat()
        } else if (type == Double::class && value is Number) {
            value = value.toDouble()
        }

        return type.safeCast(value) ?: throw IllegalArgumentException("Expected ${type.simpleName} literal")
    }
}

private class FirAnnotationArgumentProvider(val annotation: FirAnnotation, session: FirSession) : BaseFirAnnotationArgumentProvider(session) {
    init {
        if (!annotation.resolved) {
            throw IllegalStateException("Can only read resolved annotations, $annotation is not resolved")
        }
    }

    override fun renderForErrorReporting(raw: FirExpression): String {
        return raw.render()
    }

    override fun getRawValueForParameter(parameterName: String, parameterIndex: Int): FirExpression? {
        return annotation.findArgumentByName(Name.guessByFirstCharacter(parameterName), false)?.unwrapArgument()?.unwrapExpression()
    }
}

private class FirAnnotationCallArgumentProvider(val call: FirFunctionCall, session: FirSession) : BaseFirAnnotationArgumentProvider(session) {
    override fun getRawValueForParameter(parameterName: String, parameterIndex: Int): FirExpression? {
        val mapping = call.resolvedArgumentMapping ?: throw IllegalStateException("Can only read resolved annotations")
        return mapping.entries.firstOrNull { it.value.name.asString() == parameterName }?.key
    }
}
