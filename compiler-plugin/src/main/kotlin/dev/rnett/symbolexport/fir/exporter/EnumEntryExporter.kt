package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirEnumEntry
import org.jetbrains.kotlin.fir.resolve.getContainingClassSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

class EnumEntryExporter(session: FirSession, illegalUseChecker: IllegalUseCheckerImpl) : BaseSymbolExporter<FirEnumEntry>(session, illegalUseChecker) {
    override fun getParent(declaration: FirEnumEntry): FirBasedSymbol<*>? =
        declaration.getContainingClassSymbol()

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirEnumEntry): Pair<KtSourceElement?, InternalName>? {
        return declaration.source to InternalNames.enumEntryName(declaration, session)
    }
}