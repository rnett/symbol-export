package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.exporter.Helpers.createClassName
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

class ClassExporter(illegalUseChecker: IllegalUseCheckerImpl, session: FirSession) : BaseSymbolExporter<FirClass>(illegalUseChecker, session) {
    override fun getParent(declaration: FirClass): FirBasedSymbol<*>? = declaration.getContainingClassSymbol()

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirClass): Pair<KtSourceElement?, InternalName>? {
        return declaration.source to createClassName(declaration.symbol)
    }
}