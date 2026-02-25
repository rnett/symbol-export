package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.Names
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

class ValueParameterExporter(session: FirSession, illegalUseChecker: IllegalUseCheckerImpl) : BaseSymbolExporter<FirValueParameter>(session, illegalUseChecker) {
    override fun getParent(declaration: FirValueParameter): FirBasedSymbol<*>? =
        declaration.containingDeclarationSymbol

    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirValueParameter): Boolean {
        if (declaration.containingDeclarationSymbol.hasAnnotation(Names.ExportParameters, session))
            return true

        return super.shouldExportFrom(hasExportAnnotation, declaration)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirValueParameter): Pair<KtSourceElement?, InternalName>? {
        return declaration.source to InternalNames.valueOrContextParameterName(declaration)
    }
}