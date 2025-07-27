package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.Predicates
import dev.rnett.symbolexport.fir.exporter.Helpers.createMemberName
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.utils.correspondingValueParameterFromPrimaryConstructor
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

class MemberExporter<T : FirCallableDeclaration>(illegalUseChecker: IllegalUseCheckerImpl, session: FirSession) : BaseSymbolExporter<T>(illegalUseChecker, session) {
    override fun getParent(declaration: T): FirBasedSymbol<*>? = declaration.getContainingClassSymbol()

    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: T): Boolean {

        // do not include enum entries, fields, params, etc
        if (declaration !is FirProperty && declaration !is FirFunction) return false

        if (hasExportAnnotation) return true

        if (declaration is FirProperty) {
            val valueParam = declaration.correspondingValueParameterFromPrimaryConstructor ?: return false
            return context.session.predicateBasedProvider.matches(Predicates.exportPredicate, valueParam)
        }
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: T): Pair<KtSourceElement?, InternalName>? {
        return declaration.source to createMemberName(declaration.symbol)
    }
}