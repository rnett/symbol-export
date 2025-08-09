package dev.rnett.symbolexport.symbol.compiler.annotation.fir

import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameter
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType
import dev.rnett.symbolexport.symbol.annotation.AnnotationWriter
import dev.rnett.symbolexport.symbol.annotation.BaseAnnotationWriter
import dev.rnett.symbolexport.symbol.compiler.asClassId
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
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.ConstantValueKind

public fun <S : Symbol.Annotation<S, A>, A : Symbol.Annotation.Arguments<S, A>> A.toFirAnnotation(
    session: FirSession,
    source: KtSourceElement? = null,
    useSiteTarget: AnnotationUseSiteTarget? = null
): FirAnnotation = FirAnnotationWriter(session, source, useSiteTarget).write(this)

private class FirAnnotationWriter(
    val session: FirSession,
    val source: KtSourceElement? = null,
    val useSiteTarget: AnnotationUseSiteTarget? = null
) : BaseAnnotationWriter<FirAnnotation, FirExpression>() {

    override fun assembleAnnotation(annotation: Symbol.Annotation<*, *>, arguments: Map<AnnotationParameter<*>, FirExpression?>, isTopLevel: Boolean): FirAnnotation {
        return buildAnnotation {
            if (isTopLevel)
                useSiteTarget = this@FirAnnotationWriter.useSiteTarget

            val annotation =
                session.symbolProvider.getClassLikeSymbolByClassId(annotation.asClassId())
                        as? FirClassSymbol<*> ?: throw IllegalArgumentException("No class symbol found for ${annotation.asClassId()}")

            source = this@FirAnnotationWriter.source

            annotationTypeRef = annotation.constructStarProjectedType().toFirResolvedTypeRef()

            argumentMapping = buildAnnotationArgumentMapping {
                source = this@FirAnnotationWriter.source
                arguments.forEach { (param, arg) ->
                    if (arg == null) return@forEach
                    mapping[Name.guessByFirstCharacter(param.name)] = arg
                }
            }
        }
    }

    override fun writerForAnnotationArgument(annotation: Symbol.Annotation<*, *>): AnnotationWriter<FirExpression> {
        return this
    }

    override fun writeArrayArgument(
        elements: List<FirExpression>,
        elementType: AnnotationParameterType<*>
    ): FirExpression {
        return buildArrayLiteral {
            source = this@FirAnnotationWriter.source
            coneTypeOrNull = elementType.toConeType()
            argumentList = buildArgumentList {
                source = this@FirAnnotationWriter.source
                elements.forEach {
                    arguments += it
                }
            }
        }
    }

    @OptIn(DirectDeclarationsAccess::class)
    override fun writeEnumEntryArgument(enumClass: Symbol.Classifier, enumEntryName: String): FirExpression {
        val enumClassId = enumClass.asClassId()
        val enumClassSymbol = session.symbolProvider.getClassLikeSymbolByClassId(enumClassId) as? FirClassSymbol<*> ?: throw IllegalArgumentException("No class symbol found for $enumClassId")
        val enumClassType = enumClassSymbol.constructStarProjectedType()

        val enumVariantName = Name.guessByFirstCharacter(enumEntryName)
        val enumEntrySymbol = enumClassSymbol.declarationSymbols
            .filterIsInstance<FirEnumEntrySymbol>()
            .find { it.name == enumVariantName }
            ?: throw IllegalArgumentException("No enum entry symbol found for $enumVariantName in $enumClassSymbol")

        return buildPropertyAccessExpression {
            source = this@FirAnnotationWriter.source
            val receiver = buildResolvedQualifier {
                source = this@FirAnnotationWriter.source
                coneTypeOrNull = enumClassType
                packageFqName = enumClassId.packageFqName
                relativeClassFqName = enumClassId.relativeClassName
                symbol = enumClassSymbol
            }
            coneTypeOrNull = enumClassType
            calleeReference = buildResolvedNamedReference {
                source = this@FirAnnotationWriter.source
                name = enumVariantName
                resolvedSymbol = enumEntrySymbol
            }
            explicitReceiver = receiver
            dispatchReceiver = receiver
        }
    }

    override fun writeClassArgument(value: Symbol.Classifier): FirExpression {
        return buildGetClassCall {
            source = this@FirAnnotationWriter.source

            val classId = value.asClassId()
            val classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirClassSymbol<*> ?: throw IllegalArgumentException("No class symbol found for $classId")
            val classType = classSymbol.constructStarProjectedType()

            coneTypeOrNull = StandardClassIds.KClass.createConeType(session, arrayOf(classType))

            argumentList = buildUnaryArgumentList(buildResolvedQualifier {
                source = this@FirAnnotationWriter.source
                coneTypeOrNull = classType
                symbol = classSymbol
                isFullyQualified = true
                canBeValue = true
                packageFqName = classId.packageFqName
                relativeClassFqName = classId.relativeClassName
            })
        }
    }

    override fun writePrimitiveArgument(value: AnnotationArgument.Primitive<*>): FirExpression {
        return buildLiteralExpression(
            source,
            when (value) {
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


    private fun AnnotationParameterType<*>.toConeType(): ConeClassLikeType = when (this) {
        is AnnotationParameterType.Annotation<*, *> -> {
            val classId = annotationClass.asClassId()
            val classSymbol = session.symbolProvider.getClassLikeSymbolByClassId(classId) as? FirClassSymbol<*> ?: throw IllegalArgumentException("No class symbol found for $classId")
            classSymbol.constructStarProjectedType()
        }

        is AnnotationParameterType.Array<*, *> -> {
            elementType.toConeType().createArrayType()
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
}