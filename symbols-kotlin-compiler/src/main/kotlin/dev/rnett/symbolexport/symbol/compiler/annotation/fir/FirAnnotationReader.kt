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
import org.jetbrains.kotlin.fir.declarations.getTargetType
import org.jetbrains.kotlin.fir.declarations.resolved
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirArrayLiteral
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.expressions.unwrapExpression
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.name.Name
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> FirAnnotationContainer.readAnnotation(annotation: S, session: FirSession): A? {
    val value = getAnnotationByClassId(annotation.asClassId(), session) ?: return null
    return annotation.produceArguments(FirAnnotationArgumentProvider(value, session))
}

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> FirBasedSymbol<*>.readAnnotation(annotation: S, session: FirSession): A? {
    val value = getAnnotationByClassId(annotation.asClassId(), session) ?: return null
    return annotation.produceArguments(FirAnnotationArgumentProvider(value, session))
}

private class FirAnnotationArgumentProvider(val annotation: FirAnnotation, val session: FirSession) : BaseAnnotationArgumentProducer<FirExpression>() {
    init {
        if (!annotation.resolved) {
            throw IllegalStateException("Can only read resolved annotations, $annotation is not resolved")
        }
    }

    override fun renderForErrorReporting(raw: FirExpression): String {
        return raw.render()
    }

    override fun getRawValueForParameter(parameterName: String): FirExpression? {
        return annotation.findArgumentByName(Name.guessByFirstCharacter(parameterName), false)?.unwrapArgument()?.unwrapExpression()
    }

    override fun extractAnnotationProducer(expression: FirExpression, expectedAnnotation: Symbol.Annotation<*, *>): AnnotationArgumentProducer {
        val annotationExpr = expression.evaluateAs<FirAnnotation>(session) ?: throw IllegalArgumentException("Expected annotation")
        val actualClassId = annotationExpr.toAnnotationClassId(session)
        if (actualClassId != expectedAnnotation.asClassId()) {
            throw IllegalArgumentException("Expected annotation of type ${expectedAnnotation.asClassId()}, but got $actualClassId")
        }
        return FirAnnotationArgumentProvider(annotationExpr, session)
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
        return type.safeCast(literal.value) ?: throw IllegalArgumentException("Expected ${type.simpleName} literal")
    }
}
