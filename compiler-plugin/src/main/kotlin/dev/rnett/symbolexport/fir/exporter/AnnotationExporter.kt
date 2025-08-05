package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.Errors
import dev.rnett.symbolexport.fir.Predicates
import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.resolve.toClassSymbol
import org.jetbrains.kotlin.fir.resolve.toRegularClassSymbol
import org.jetbrains.kotlin.fir.scopes.collectAllProperties
import org.jetbrains.kotlin.fir.scopes.impl.declaredMemberScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.arrayElementType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.isArrayType
import org.jetbrains.kotlin.fir.types.isKClassType
import org.jetbrains.kotlin.name.StandardClassIds

class AnnotationExporter(session: FirSession, illegalUseChecker: IllegalUseChecker) : BaseSymbolExporter<FirClass>(session, illegalUseChecker) {
    override fun getParent(declaration: FirClass): FirBasedSymbol<*>? {
        return declaration.getContainingClassSymbol()
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun additionalChecks(declaration: FirClass): Boolean {

        if (declaration.classKind != ClassKind.ANNOTATION_CLASS) {
            reporter.reportOn(declaration.source, Errors.SYMBOL_EXPORT_EXPORTED_ANNOTATION_NOT_AN_ANNOTATION_CLASS)
            return false
        }

        val properties = session.declaredMemberScope(declaration, null).collectAllProperties()
        properties.forEach {
            if (session.predicateBasedProvider.matches(Predicates.export, it)) {
                reporter.reportOn(it.source, Errors.SYMBOL_EXPORT_EXPORTING_FROM_EXPORTED_ANNOTATION)
            }
        }

        if (session.predicateBasedProvider.matches(Predicates.export, declaration) || session.predicateBasedProvider.matches(Predicates.childrenExported, declaration)) {
            reporter.reportOn(declaration.source, Errors.SYMBOL_EXPORT_EXPORTING_EXPORTED_ANNOTATION)
        }

        val unmarkedAnnotationProperties = properties
            .mapNotNull { prop -> prop.resolvedReturnType.toClassSymbol(session)?.let { prop to it } }
            .filter {
                it.second.classKind == ClassKind.ANNOTATION_CLASS
            }
            .filter {
                !session.predicateBasedProvider.matches(Predicates.annotationExport, it.second)
            }

        if (unmarkedAnnotationProperties.isNotEmpty()) {
            unmarkedAnnotationProperties.forEach { (prop, _) ->
                reporter.reportOn(prop.source, Errors.SYMBOL_EXPORT_EXPORTED_ANNOTATION_ANNOTATION_PROPERTY_NOT_EXPORTED)
            }
            return false
        }

        return true
    }

    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirClass): Boolean {
        return session.predicateBasedProvider.matches(Predicates.annotationExport, declaration)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirClass): Pair<KtSourceElement?, InternalName>? {

        val classifierName = Helpers.createClassName(declaration.symbol)

        val params = session.declaredMemberScope(declaration, null).collectAllProperties()
            .associate { it.name.asString() to getAnnotationParamType(it.source, it.resolvedReturnType) }

        if (params.values.any { it == null }) {
            return null
        }

        return declaration.source to InternalName.Annotation(
            classifierName.packageName,
            classifierName.classNames,
            params.mapValues { it.value!! }
        )
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun getAnnotationParamType(source: KtSourceElement?, type: ConeKotlinType): AnnotationParameterType? {
        if (type.isArrayType) {
            val element = type.arrayElementType() ?: error("Array element type should not be null")
            return AnnotationParameterType.Array(getAnnotationParamType(source, element) ?: return null)
        }
        if (type.toRegularClassSymbol(session)?.isEnumClass == true) {
            return AnnotationParameterType.Enum(Helpers.createClassName(type.toClassSymbol(session)!!))
        }
        if (type.toClassSymbol(session)?.classKind == ClassKind.ANNOTATION_CLASS) {
            return AnnotationParameterType.Annotation(Helpers.createClassName(type.toClassSymbol(session)!!))
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

        reporter.reportOn(source, Errors.SYMBOL_EXPORT_UNSUPPORTED_ANNOTATION_PROPERTY_TYPE, type)
        return null
    }
}