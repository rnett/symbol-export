package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.Names
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.toAnnotationClassId
import org.jetbrains.kotlin.fir.dispatchReceiverClassTypeOrNull
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.customAnnotations
import org.jetbrains.kotlin.fir.types.typeAnnotations

class DispatchReceiverExporter<T : FirCallableDeclaration>(session: FirSession, illegalUseChecker: IllegalUseCheckerImpl) : BaseSymbolExporter<T>(session, illegalUseChecker) {
    // we're exporting a child
    override fun getParent(declaration: T): FirBasedSymbol<*>? = declaration.symbol

    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: T): Boolean {

        // we ignore the export annotation here, since it's on the function, not the receiver

        val receiver = declaration.dispatchReceiverClassTypeOrNull() ?: return false


        if (receiver.typeAnnotations.any { it.toAnnotationClassId(session) == Names.EXPORT_ANNOTATION_CLASSID } || receiver.customAnnotations.any {
                it.toAnnotationClassId(
                    session
                ) == Names.EXPORT_ANNOTATION_CLASSID
            })
            return true

        if (declaration.hasAnnotation(Names.ExportParameters, session)) {
            return true
        }

        val exportReceiverAnnotation = declaration.getAnnotationByClassId(
            Names.EXPORT_RECEIVERS_ANNOTATION_CLASSID,
            session
        )

        if (exportReceiverAnnotation != null) {
            if (exportReceiverAnnotation.getBooleanArgument(Names.EXPORT_RECEIVERS_DISPATCH_PROP, session) != false)
                return true

        }
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: T): Pair<KtSourceElement?, InternalName>? {
        return InternalNames.dispatchReceiverName(declaration.symbol)?.let { declaration.source to it }
    }
}