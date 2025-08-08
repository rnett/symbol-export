package dev.rnett.symbolexport.symbol.compiler.annotation

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameter
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType
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

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> FirAnnotationContainer.readAnnotation(annotation: S, session: FirSession): A? {
    val value = getAnnotationByClassId(annotation.asClassId(), session) ?: return null
    return annotation.produceArguments(FirAnnotationArgumentProvider(value, session))
}

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> FirBasedSymbol<*>.readAnnotation(annotation: S, session: FirSession): A? {
    val value = getAnnotationByClassId(annotation.asClassId(), session) ?: return null
    return annotation.produceArguments(FirAnnotationArgumentProvider(value, session))
}

public class AnnotationArgumentException(message: String) : RuntimeException(message)

public class AnnotationParameterException(public val parameter: AnnotationParameter<*>, public val actualValue: String, cause: Throwable) :
    RuntimeException("Error evaluating annotation parameter ${parameter.name} with expected type ${parameter.type}, actual value was $actualValue", cause)

//TODO test

private class FirAnnotationArgumentProvider(val annotation: FirAnnotation, val session: FirSession) : AnnotationArgumentProducer {
    init {
        if (!annotation.resolved) {
            throw IllegalStateException("Can only read resolved annotations, $annotation is not resolved")
        }
    }

    override fun <T : AnnotationArgument, P : AnnotationParameterType<T>> getArgument(parameter: AnnotationParameter<P>): T? {
        val value = annotation.findArgumentByName(Name.guessByFirstCharacter(parameter.name), false)?.unwrapArgument()?.unwrapExpression() ?: return null
        @Suppress("UNCHECKED_CAST")
        return try {
            asArgumentType(value, parameter.type)
        } catch (e: AnnotationArgumentException) {
            throw AnnotationParameterException(parameter, value.render(), e)
        } catch (e: AnnotationParameterException) {
            throw AnnotationParameterException(parameter, value.render(), e)
        }
    }

    private fun <T : AnnotationArgument, P : AnnotationParameterType<T>> asArgumentType(expression: FirExpression, type: P): T {
        return when (type) {
            is AnnotationParameterType.Annotation<*, *> -> {
                val annotation = expression.evaluateAs<FirAnnotation>(session) ?: throw AnnotationArgumentException("Expected annotation")
                if (annotation.toAnnotationClassId(session) != type.annotationClass.asClassId()) {
                    throw AnnotationArgumentException("Expected annotation of type ${type.annotationClass.asClassId()}")
                }
                val producer = FirAnnotationArgumentProvider(annotation, session)

                AnnotationArgument.Annotation(type.annotationClass.produceArguments(producer)) as T
            }

            is AnnotationParameterType.Array<*, *> -> {
                val arrayLiteral = expression.evaluateAs<FirArrayLiteral>(session) ?: throw AnnotationArgumentException("Expected array literal")
                val elements = arrayLiteral.arguments

                @Suppress("UNCHECKED_CAST")
                AnnotationArgument.Array(elements.map { asArgumentType(it, type.elementType)!! }, type.elementType) as T
            }

            is AnnotationParameterType.Enum -> {
                val enum = expression.extractEnumValueArgumentInfo() ?: throw AnnotationArgumentException("Expected enum value")
                return AnnotationArgument.EnumEntry(
                    enum.enumClassId?.toClassifier() ?: throw AnnotationArgumentException("Enum class not found"),
                    enum.enumEntryName.asString()
                ) as T
            }

            AnnotationParameterType.KClass -> {
                val getClassCall = expression.evaluateAs<FirGetClassCall>(session) ?: throw AnnotationArgumentException("Expected class literal}")
                val classId = getClassCall.getTargetType()?.classId ?: throw AnnotationArgumentException("Expected class literal")

                AnnotationArgument.KClass(classId.toClassifier()) as T
            }

            AnnotationParameterType.Boolean -> getPrimitiveArgumentValue<Boolean>(expression, session).let { AnnotationArgument.Boolean(it) as T }
            AnnotationParameterType.Byte -> getPrimitiveArgumentValue<Byte>(expression, session).let { AnnotationArgument.Byte(it) as T }
            AnnotationParameterType.Char -> getPrimitiveArgumentValue<Char>(expression, session).let { AnnotationArgument.Char(it) as T }
            AnnotationParameterType.Double -> getPrimitiveArgumentValue<Double>(expression, session).let { AnnotationArgument.Double(it) as T }
            AnnotationParameterType.Float -> getPrimitiveArgumentValue<Float>(expression, session).let { AnnotationArgument.Float(it) as T }
            AnnotationParameterType.Int -> getPrimitiveArgumentValue<Int>(expression, session).let { AnnotationArgument.Int(it) as T }
            AnnotationParameterType.Long -> getPrimitiveArgumentValue<Long>(expression, session).let { AnnotationArgument.Long(it) as T }
            AnnotationParameterType.Short -> getPrimitiveArgumentValue<Short>(expression, session).let { AnnotationArgument.Short(it) as T }
            AnnotationParameterType.String -> getPrimitiveArgumentValue<String>(expression, session).let { AnnotationArgument.String(it) as T }
        }
    }
}

private inline fun <reified T> getPrimitiveArgumentValue(argument: FirExpression, session: FirSession): T {
    val literal = argument.evaluateAs<FirLiteralExpression>(session) ?: throw AnnotationArgumentException("Expected literal")
    return literal.value as? T ?: throw AnnotationArgumentException("Expected ${T::class.simpleName} literal")
}
