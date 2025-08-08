package dev.rnett.symbolexport.symbol.compiler.annotation

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType
import dev.rnett.symbolexport.symbol.compiler.asClassId
import dev.rnett.symbolexport.symbol.compiler.toClassifier
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.annotations.AnnotationUseSiteTarget
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.buildUnaryArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildArrayLiteral
import org.jetbrains.kotlin.fir.expressions.builder.buildGetClassCall
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildResolvedQualifier
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirEnumEntrySymbol
import org.jetbrains.kotlin.fir.toFirResolvedTypeRef
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.constructStarProjectedType
import org.jetbrains.kotlin.fir.types.createArrayType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.ConstantValueKind

public fun AnnotationArgument.Companion.kClass(classId: ClassId): AnnotationArgument.KClass = AnnotationArgument.kClass(classId.toClassifier())
public fun AnnotationArgument.Companion.kClass(symbol: FirClassSymbol<*>): AnnotationArgument.KClass = AnnotationArgument.kClass(symbol.classId)

public fun AnnotationArgument.Companion.enum(classId: ClassId, name: String): AnnotationArgument.EnumEntry =
    AnnotationArgument.enum(classId.toClassifier(), name)

public fun AnnotationArgument.Companion.enum(symbol: FirClassSymbol<*>, name: String): AnnotationArgument.EnumEntry =
    AnnotationArgument.enum(symbol.classId, name)

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> A.toAnnotation(
    session: FirSession,
    source: KtSourceElement? = null,
    useSiteTarget: AnnotationUseSiteTarget? = null
): FirAnnotation = buildAnnotation {
    this.useSiteTarget = useSiteTarget
    val annotation =
        session.symbolProvider.getClassLikeSymbolByClassId(annotation.asClassId())
                as? FirClassSymbol<*> ?: error("No class symbol found for ${annotation.asClassId()}")

    this.source = source

    annotationTypeRef = annotation.constructStarProjectedType().toFirResolvedTypeRef()

    argumentMapping = buildAnnotationArgumentMapping {
        this.source = source
        this@toAnnotation.asMap.forEach { (param, arg) ->
            mapping[Name.guessByFirstCharacter(param.name)] = arg.toFirExpression(session, source)
        }
    }
}

private fun AnnotationParameterType<*>.toConeType(session: FirSession): ConeClassLikeType = when (this) {
    is AnnotationParameterType.Annotation<*, *> -> {
        val classId = annotationClass.asClassId()
        val classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirClassSymbol<*> ?: error("No class symbol found for $classId")
        classSymbol.constructStarProjectedType()
    }

    is AnnotationParameterType.Array<*, *> -> {
        elementType.toConeType(session).createArrayType()
    }

    is AnnotationParameterType.Enum -> enumClass.asClassId().createConeType(session, arrayOf())
    AnnotationParameterType.KClass -> StandardClassIds.KClass.createConeType(session, arrayOf(ConeStarProjection)).createArrayType()
    AnnotationParameterType.Boolean -> session.builtinTypes.booleanType.coneType
    AnnotationParameterType.Byte -> session.builtinTypes.byteType.coneType
    AnnotationParameterType.Char -> session.builtinTypes.charType.coneType
    AnnotationParameterType.Double -> session.builtinTypes.doubleType.coneType
    AnnotationParameterType.Float -> session.builtinTypes.floatType.coneType
    AnnotationParameterType.Int -> session.builtinTypes.intType.coneType
    AnnotationParameterType.Long -> session.builtinTypes.longType.coneType
    AnnotationParameterType.Short -> session.builtinTypes.shortType.coneType
    AnnotationParameterType.String -> session.builtinTypes.stringType.coneType
}

@OptIn(DirectDeclarationsAccess::class)
private fun AnnotationArgument.toFirExpression(session: FirSession, source: KtSourceElement?): FirExpression = when (this) {
    is AnnotationArgument.Annotation<*, *> -> annotationArguments.toAnnotation(session, source)
    is AnnotationArgument.Array<*> -> buildArrayLiteral {
        this.source = source
        coneTypeOrNull = elementType.toConeType(session)
        argumentList = buildArgumentList {
            this.source = source
            this@toFirExpression.values.forEach {
                arguments += it.toFirExpression(session, source)
            }
        }
    }

    is AnnotationArgument.EnumEntry -> {
        val enumClassId = this@toFirExpression.enumClass.asClassId()
        val enumClassSymbol = session.symbolProvider.getClassLikeSymbolByClassId(enumClassId) as? FirClassSymbol<*> ?: error("No class symbol found for $enumClassId")
        val enumClassType = enumClassSymbol.constructStarProjectedType()

        val enumVariantName = Name.guessByFirstCharacter(this.enumName)
        val enumEntrySymbol = enumClassSymbol.declarationSymbols
            .filterIsInstance<FirEnumEntrySymbol>()
            .find { it.name == enumVariantName }
            ?: error("No enum entry symbol found for $enumVariantName in $enumClassSymbol")

        buildPropertyAccessExpression {
            this.source = source
            val receiver = buildResolvedQualifier {
                this.source = source
                coneTypeOrNull = enumClassType
                packageFqName = enumClassId.packageFqName
                relativeClassFqName = enumClassId.relativeClassName
                symbol = enumClassSymbol
            }
            coneTypeOrNull = enumClassType
            calleeReference = buildResolvedNamedReference {
                this.source = source
                name = enumVariantName
                resolvedSymbol = enumEntrySymbol
            }
            explicitReceiver = receiver
            dispatchReceiver = receiver
        }
    }

    is AnnotationArgument.KClass -> buildGetClassCall {
        this.source = source

        val classId = this@toFirExpression.classSymbol.asClassId()
        val classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirClassSymbol<*> ?: error("No class symbol found for $classId")
        val classType = classSymbol.constructStarProjectedType()

        coneTypeOrNull = StandardClassIds.KClass.createConeType(session, arrayOf(classType))

        argumentList = buildUnaryArgumentList(buildResolvedQualifier {
            this.source = source
            coneTypeOrNull = classType
            symbol = classSymbol
            isFullyQualified = true
            canBeValue = true
            packageFqName = classId.packageFqName
            relativeClassFqName = classId.relativeClassName
        })
    }

    is AnnotationArgument.Primitive<*> -> buildLiteralExpression(
        source,
        when (this) {
            is AnnotationArgument.Boolean -> ConstantValueKind.Boolean
            is AnnotationArgument.Byte -> ConstantValueKind.Byte
            is AnnotationArgument.Char -> ConstantValueKind.Char
            is AnnotationArgument.Double -> ConstantValueKind.Double
            is AnnotationArgument.Float -> ConstantValueKind.Float
            is AnnotationArgument.Int -> ConstantValueKind.Int
            is AnnotationArgument.Long -> ConstantValueKind.Long
            is AnnotationArgument.Short -> ConstantValueKind.Short
            is AnnotationArgument.String -> ConstantValueKind.String
        },
        value,
        setType = true
    )
}