package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.Predicates
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

abstract class BaseSymbolExporter<T : FirDeclaration>(
    val illegalUseChecker: IllegalUseChecker,
    session: FirSession
) : SymbolExporter<T>(session) {

    abstract override fun getParent(declaration: T): FirBasedSymbol<*>?

    context(context: CheckerContext)
    open fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: T): Boolean = hasExportAnnotation

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun exportSymbols(declaration: T): Iterable<Pair<KtSourceElement?, InternalName>> {

        if (declaration is FirMemberDeclaration && declaration.isActual) {
            return emptyList()
        }

        val isAnnotated = session.predicateBasedProvider.matches(Predicates.exportPredicate, declaration)

        if (!shouldExportFrom(isAnnotated, declaration)) {
            return emptyList()
        }

        if (illegalUseChecker.checkIllegalUse(declaration, this)) {
            return emptyList()
        }


        createName(declaration)?.let {
            return listOf(it)
        }
        return emptyList()
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    abstract fun createName(declaration: T): Pair<KtSourceElement?, InternalName>?
}