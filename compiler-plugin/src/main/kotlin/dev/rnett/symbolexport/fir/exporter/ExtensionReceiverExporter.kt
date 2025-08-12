package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.Names
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirReceiverParameter
import org.jetbrains.kotlin.fir.declarations.getAnnotationByClassId
import org.jetbrains.kotlin.fir.declarations.getBooleanArgument
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

class ExtensionReceiverExporter(session: FirSession, illegalUseChecker: IllegalUseCheckerImpl) : BaseSymbolExporter<FirReceiverParameter>(session, illegalUseChecker) {
    override fun getParent(declaration: FirReceiverParameter): FirBasedSymbol<*>? =
        declaration.containingDeclarationSymbol

    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirReceiverParameter): Boolean {
        if (hasExportAnnotation)
            return true

        if (declaration.hasAnnotation(Names.EXPORT_ANNOTATION_CLASSID, session))
            return true

        if (declaration.typeRef.hasAnnotation(Names.EXPORT_ANNOTATION_CLASSID, session))
            return true

        val exportReceiverAnnotation = declaration.containingDeclarationSymbol.getAnnotationByClassId(
            Names.EXPORT_RECEIVERS_ANNOTATION_CLASSID,
            session
        )

        if (exportReceiverAnnotation != null) {
            if (exportReceiverAnnotation.getBooleanArgument(Names.EXPORT_RECEIVERS_EXTENSION_PROP, session) != false)
                return true

        }
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirReceiverParameter): Pair<KtSourceElement?, InternalName>? {
        return declaration.source to InternalNames.extensionReceiverName(declaration)
    }
}