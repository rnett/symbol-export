package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.exporter.Helpers.createClassName
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.FirEnumEntry
import org.jetbrains.kotlin.fir.declarations.collectEnumEntries
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol

class EnumEntryExporter(session: FirSession, illegalUseChecker: IllegalUseCheckerImpl) : BaseSymbolExporter<FirEnumEntry>(session, illegalUseChecker) {
    override fun getParent(declaration: FirEnumEntry): FirBasedSymbol<*>? =
        declaration.getContainingClassSymbol()

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirEnumEntry): Pair<KtSourceElement?, InternalName>? {

        val ordinal = (declaration.getContainingClassSymbol()!! as FirClassSymbol<*>).collectEnumEntries(session).indexOf(declaration.symbol)

        if (ordinal < 0) error("Could not find $declaration in ${declaration.getContainingClassSymbol()}")

        return declaration.source to InternalName.EnumEntry(createClassName(declaration.getContainingClassSymbol()!!), declaration.name.asString(), ordinal)
    }
}