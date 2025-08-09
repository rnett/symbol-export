package dev.rnett.symbolexport.symbol.ksp.annotation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import dev.rnett.symbolexport.symbol.NameSegments
import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.annotation.BaseAnnotationArgumentProducer
import dev.rnett.symbolexport.symbol.ksp.matches
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> KSAnnotated.findAnnotations(annotation: S, checkResolvedTypes: Boolean = true): List<A> =
    annotations.filter { it.shortName.asString() == annotation.name && (!checkResolvedTypes || it.annotationType.resolve().declaration.matches(annotation)) }.mapNotNull { it.readAnnotation(annotation, checkResolvedTypes) }.toList()

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> KSAnnotated.findAnnotation(annotation: S, checkResolvedTypes: Boolean = true): A? {
    val value = annotations.firstOrNull { it.shortName.asString() == annotation.name && (!checkResolvedTypes || it.annotationType.resolve().declaration.matches(annotation)) }
        ?: return null
    return value.readAnnotation(annotation, checkResolvedTypes)
}

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> KSAnnotation.readAnnotation(annotation: S, checkResolvedTypes: Boolean = true): A? {
    if (shortName.asString() != annotation.name)
        return null

    if (checkResolvedTypes && !annotationType.resolve().declaration.matches(annotation))
        return null

    return annotation.produceArguments(KspAnnotationReader(this, checkResolvedTypes))
}

/**
 * The Any type parameter matches [KSValueArgument.value]
 */
private class KspAnnotationReader(val annotation: KSAnnotation, val checkResolvedTypes: Boolean) : BaseAnnotationArgumentProducer<Any>() {
    val unnamedArguments by lazy { annotation.arguments.filter { it.name == null } }
    val namedArguments by lazy { annotation.arguments.filter { it.name != null }.associateBy { it.name!!.asString() } }

    init {
        var seenNamed = false
        annotation.arguments.forEach {
            if (it.name != null)
                seenNamed = true
            else if (seenNamed)
                throw IllegalStateException("Unnamed argument after named argument")
        }
    }

    override fun renderForErrorReporting(raw: Any): String {
        return raw.toString()
    }

    override fun getRawValueForParameter(parameterName: String, parameterIndex: Int): KSValueArgument? {
        return namedArguments[parameterName] ?: unnamedArguments.getOrNull(parameterIndex)
    }

    override fun extractAnnotationProducer(
        expression: Any,
        expectedAnnotation: Symbol.Annotation<*, *>
    ): AnnotationArgumentProducer {
        if (expression !is KSAnnotation)
            throw IllegalArgumentException("Expected annotation for annotation argument")

        if (expression.shortName.asString() != expectedAnnotation.name)
            throw IllegalArgumentException("Expected annotation with name ${expectedAnnotation.name}, got ${expression.shortName.asString()}")

        if (checkResolvedTypes) {
            val resolved = expression.annotationType.resolve()
            if (!resolved.declaration.matches(expectedAnnotation))
                throw IllegalArgumentException("Expected annotation with qualified name ${expectedAnnotation.asString()}, got ${resolved.declaration.qualifiedName?.asString()}")
        }

        return KspAnnotationReader(expression, checkResolvedTypes)
    }

    override fun extractArrayArguments(expression: Any): List<Any> {
        if (expression !is Array<*>)
            throw IllegalArgumentException("Expected array for array argument")

        for (e in expression) {
            if (e == null)
                throw IllegalArgumentException("Null values are not allowed in array arguments")
        }

        return expression.asList() as List<Any>
    }

    override fun extractEnumInfo(expression: Any): AnnotationArgument.EnumEntry {
        if (expression !is KSClassDeclaration)
            throw IllegalArgumentException("Expected enum entry for enum argument")

        if (expression.classKind != ClassKind.ENUM_ENTRY)
            throw IllegalArgumentException("Expected enum entry class for enum argument")

        return AnnotationArgument.EnumEntry(
            expression.toClassifier(),
            expression.simpleName.asString()
        )
    }

    override fun extractClass(expression: Any): Symbol.Classifier {
        if (expression !is KSType)
            throw IllegalArgumentException("Expected class for class argument")
        val declaration = expression.declaration as? KSClassDeclaration ?: throw IllegalArgumentException("Expected class for class argument")
        return declaration.toClassifier()
    }

    override fun <T : Any> extractPrimitiveValue(expression: Any, type: KClass<T>): T {
        return type.safeCast(expression) ?: throw IllegalArgumentException("Expected value of type ${type.simpleName}, got ${expression::class} for $expression")
    }
}

private fun KSClassDeclaration.toClassifier(): Symbol.Classifier {
    val packageName = NameSegments(packageName.asString().split("."))
    val allNames = qualifiedName?.asString()?.split(".") ?: throw IllegalStateException("Qualified name should not be null")
    val classNames = NameSegments(allNames.drop(packageName.nameSegments.size))
    return Symbol.Classifier(packageName, classNames)
}