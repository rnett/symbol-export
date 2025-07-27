package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.NameReporter
import dev.rnett.symbolexport.fir.Errors
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

abstract class SymbolExporter<T : FirDeclaration>(val session: FirSession) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    abstract fun exportSymbols(declaration: T): Iterable<Pair<KtSourceElement?, InternalName>>


    open fun getParent(declaration: T): FirBasedSymbol<*>? {
        return null
    }
}

class SymbolExporterChecker<T : FirDeclaration>(val exporter: SymbolExporter<T>, val nameReporter: NameReporter, val reportNameDiagnostics: Boolean) : FirDeclarationChecker<T>(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: T) {
        val names = exporter.exportSymbols(declaration)
        names.forEach {
            if (reportNameDiagnostics) {
                reporter.reportOn(it.first, Errors.symbolExportMarker(it.second), it.second)
            }
            nameReporter.reportName(it.second)
        }
    }
}

fun <T : FirDeclaration> exporterCheckersOf(nameReporter: NameReporter, reportNameDiagnostics: Boolean, vararg exporters: SymbolExporter<T>): Set<SymbolExporterChecker<T>> {
    return exporters.map { SymbolExporterChecker(it, nameReporter, reportNameDiagnostics) }.toSet()
}