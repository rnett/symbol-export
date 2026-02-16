package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.Predicates
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

class ClassExporter(session: FirSession, illegalUseChecker: IllegalUseCheckerImpl) : BaseSymbolExporter<FirClass>(session, illegalUseChecker) {
    override fun getParent(declaration: FirClass): FirBasedSymbol<*>? = declaration.getContainingClassSymbol()

    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirClass): Boolean {
        return hasExportAnnotation && !session.predicateBasedProvider.matches(Predicates.annotationExport, declaration)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirClass): Pair<KtSourceElement?, InternalName>? {
        return declaration.source to InternalNames.className(declaration.symbol)
    }
}