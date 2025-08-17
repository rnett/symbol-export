package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.NameReporter
import dev.rnett.symbolexport.fir.Diagnostics
import dev.rnett.symbolexport.fir.Predicates
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

interface SymbolExporter<T : FirDeclaration> {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    fun exportSymbols(declaration: T): Iterable<Pair<KtSourceElement?, InternalName>>
}

abstract class BaseSymbolExporter<T : FirDeclaration>(
    val session: FirSession,
    val illegalUseChecker: IllegalUseChecker,
) : SymbolExporter<T> {
    abstract fun getParent(declaration: T): FirBasedSymbol<*>?

    context(context: CheckerContext)
    open fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: T): Boolean = hasExportAnnotation

    /**
     * Checked before anything else.  If it returns false, returns immediately.
     */
    context(context: CheckerContext, reporter: DiagnosticReporter)
    open fun additionalChecks(declaration: T): Boolean {
        return true
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun exportSymbols(declaration: T): Iterable<Pair<KtSourceElement?, InternalName>> {

        if (declaration is FirMemberDeclaration && declaration.isActual) {
            return emptyList()
        }

        val isAnnotated = session.predicateBasedProvider.matches(Predicates.export, declaration)

        if (!shouldExportFrom(isAnnotated, declaration)) {
            return emptyList()
        }

        if (!additionalChecks(declaration)) {
            return emptyList()
        }

        if (illegalUseChecker.checkIllegalUse(declaration, this)) {
            return emptyList()
        }

        return createAdditionalNames(declaration) + listOfNotNull(createName(declaration))
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    open fun createName(declaration: T): Pair<KtSourceElement?, InternalName>? {
        return null
    }


    context(context: CheckerContext, reporter: DiagnosticReporter)
    open fun createAdditionalNames(declaration: T): Iterable<Pair<KtSourceElement?, InternalName>> = emptyList()

}

class SymbolExporterChecker<T : FirDeclaration>(val exporter: SymbolExporter<T>, val nameReporter: NameReporter, val reportNameDiagnostics: Boolean) : FirDeclarationChecker<T>(MppCheckerKind.Common) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: T) {
        val names = exporter.exportSymbols(declaration)
        names.forEach {
            if (reportNameDiagnostics) {
                reporter.reportOn(it.first, Diagnostics.symbolExportMarker(it.second), it.second)
            }
            nameReporter.reportName(it.second)
        }
    }
}

fun <T : FirDeclaration> exporterCheckersOf(nameReporter: NameReporter, reportNameDiagnostics: Boolean, vararg exporters: SymbolExporter<T>): Set<SymbolExporterChecker<T>> {
    return exporters.map { SymbolExporterChecker(it, nameReporter, reportNameDiagnostics) }.toSet()
}