package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.exporter.Helpers.getIndexStartOffset
import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirEnumEntry
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameterKind
import org.jetbrains.kotlin.fir.declarations.collectEnumEntries
import org.jetbrains.kotlin.fir.declarations.primaryConstructorIfAny
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.resolve.toClassSymbol
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.arrayElementType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isArrayType
import org.jetbrains.kotlin.fir.types.isKClassType
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.name.StandardClassIds

object InternalNames {

    fun className(classSymbol: FirClassLikeSymbol<*>): InternalName.Classifier {
        return InternalName.Classifier(
            classSymbol.classId.packageFqName.pathSegments().map { it.asString() },
            classSymbol.classId.relativeClassName.pathSegments().map { it.asString() }
        )
    }

    fun memberName(symbol: FirCallableSymbol<*>): InternalName.Member {
        val parent = symbol.getContainingClassSymbol()

        if (symbol is FirConstructorSymbol) {
            return InternalName.Constructor(className(parent!!), SpecialNames.INIT.asString())
        }

        return if (parent == null) {
            InternalName.TopLevelMember(
                symbol.packageFqName().pathSegments().map { it.asString() },
                symbol.name.asString()
            )
        } else {
            InternalName.ClassifierMember(
                className(parent),
                symbol.name.asString()
            )
        }
    }

    fun dispatchReceiverName(functionSymbol: FirCallableSymbol<*>): InternalName.ReceiverParameter? {

        if (functionSymbol.dispatchReceiverType == null)
            return null

        val parentName = memberName(functionSymbol)

        return InternalName.ReceiverParameter(
            parentName,
            SpecialNames.THIS.asString(),
            getIndexStartOffset(functionSymbol, DISPATCH),
            DISPATCH
        )
    }

    fun extensionReceiverName(receiver: FirReceiverParameter): InternalName.ReceiverParameter {
        val parent = receiver.symbol.containingDeclarationSymbol as FirCallableSymbol<*>
        val parentName = memberName(parent)

        return InternalName.ReceiverParameter(
            parentName,
            SpecialNames.RECEIVER.asString(),
            getIndexStartOffset(parent, EXTENSION),
            EXTENSION
        )
    }

    fun valueOrContextParameterName(declaration: FirValueParameter): InternalName.IndexedParameter {
        val parent = declaration.symbol.containingDeclarationSymbol as FirCallableSymbol<*>
        val parentName = memberName(parent)

        val type = when (declaration.valueParameterKind) {
            FirValueParameterKind.Regular -> VALUE
            FirValueParameterKind.ContextParameter -> CONTEXT
            FirValueParameterKind.LegacyContextReceiver -> {
                error("Cannot handle context receivers, only context parameters")
            }
        }

        val indexInList = when (type) {
            VALUE -> {
                parent as FirFunctionSymbol<*>
                parent.valueParameterSymbols.indexOf(declaration.symbol)
            }

            CONTEXT -> parent.contextParameterSymbols.indexOf(declaration.symbol)
        }
        if (indexInList < 0) error("Could not find $declaration in $parent")

        return InternalName.IndexedParameter(
            parentName,
            declaration.name.asString(),
            getIndexStartOffset(parent, type) + indexInList,
            indexInList,
            type
        )
    }

    fun enumEntryName(declaration: FirEnumEntry, session: FirSession): InternalName.EnumEntry {
        val ordinal = (declaration.getContainingClassSymbol()!! as FirClassSymbol<*>).collectEnumEntries(session).indexOf(declaration.symbol)

        if (ordinal < 0) error("Could not find $declaration in ${declaration.getContainingClassSymbol()}")

        return InternalName.EnumEntry(className(declaration.getContainingClassSymbol()!!), declaration.name.asString(), ordinal)
    }

    fun typeParameterName(declaration: FirTypeParameter): InternalName.TypeParameter {
        val parent = declaration.symbol.containingDeclarationSymbol

        val parentName: InternalName
        val indexInList: Int

        if (parent is FirCallableSymbol<*>) {
            parentName = memberName(parent)
            indexInList = parent.typeParameterSymbols.indexOf(declaration.symbol)
        } else if (parent is FirClassSymbol<*>) {
            parentName = className(parent)
            indexInList = parent.typeParameterSymbols.indexOf(declaration.symbol)
        } else {
            error("Unknown parent type $parent - expected only classes and callables to have type parameters")
        }

        if (indexInList < 0) error("Could not find $declaration in $parent")

        return InternalName.TypeParameter(
            parentName,
            declaration.name.asString(),
            indexInList
        )
    }

    @OptIn(SymbolInternals::class)
    fun annotationName(declaration: FirClass, session: FirSession): InternalName.Annotation {
        val classifierName = className(declaration.symbol)

        val primaryConstructor = declaration.primaryConstructorIfAny(session) ?: error("Annotations must have a primary constructor")

        val params = primaryConstructor.fir.valueParameters
            .mapIndexed { idx, it ->
                val type = getAnnotationParamType(it.returnTypeRef.coneType, session)
                InternalName.Annotation.Parameter(it.name.asString(), idx, type)
            }

        return InternalName.Annotation(
            classifierName.packageName,
            classifierName.classNames,
            params
        )
    }

    private fun getAnnotationParamType(type: ConeKotlinType, session: FirSession): AnnotationParameterType {
        if (type.isArrayType) {
            val element = type.arrayElementType() ?: error("Array element type should not be null")
            return AnnotationParameterType.Array(getAnnotationParamType(element, session))
        }
        if (type.toRegularClassSymbol(session)?.isEnumClass == true) {
            return AnnotationParameterType.Enum(InternalNames.className(type.toClassSymbol(session)!!))
        }
        if (type.toClassSymbol(session)?.classKind == ClassKind.ANNOTATION_CLASS) {
            return AnnotationParameterType.Annotation(InternalNames.className(type.toClassSymbol(session)!!))
        }
        if (type.isKClassType()) {
            return AnnotationParameterType.KClass
        }

        when (type.classId) {
            StandardClassIds.String -> return AnnotationParameterType.Primitive.STRING
            StandardClassIds.Boolean -> return AnnotationParameterType.Primitive.BOOLEAN
            StandardClassIds.Int -> return AnnotationParameterType.Primitive.INT
            StandardClassIds.Float -> return AnnotationParameterType.Primitive.FLOAT
            StandardClassIds.Long -> return AnnotationParameterType.Primitive.LONG
            StandardClassIds.Double -> return AnnotationParameterType.Primitive.DOUBLE
            StandardClassIds.Byte -> return AnnotationParameterType.Primitive.BYTE
            StandardClassIds.Char -> return AnnotationParameterType.Primitive.CHAR
            StandardClassIds.Short -> return AnnotationParameterType.Primitive.SHORT
        }

        error("Unknown annotation parameter type $type")
    }

}