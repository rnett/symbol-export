package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.Names
import dev.rnett.symbolexport.fir.exporter.IllegalUseChecker
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirBasicDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.isLocalMember
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.effectiveVisibility
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.resolve.transformers.publishedApiEffectiveVisibility
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import java.util.concurrent.ConcurrentHashMap

class IllegalUseCheckerImpl(val session: FirSession) : FirBasicDeclarationChecker(MppCheckerKind.Common), IllegalUseChecker {

    private val illegalUses = ConcurrentHashMap<FirBasedSymbol<*>, Boolean?>()

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    fun isIllegalUse(declaration: FirDeclaration): Boolean = illegalUses.computeIfAbsent(declaration.symbol) { checkIllegalUse(declaration) } ?: false

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkIllegalUse(declaration: FirDeclaration): Boolean? {
        if (
            !session.predicateBasedProvider.matches(Predicates.export, declaration) &&
            !declaration.hasAnnotation(Names.ExportSymbol, session) &&
            !session.predicateBasedProvider.matches(Predicates.childrenExported, declaration) &&
            !declaration.hasAnnotation(Names.ChildrenExported, session) &&
            !session.predicateBasedProvider.matches(Predicates.declarationExport, declaration) &&
            !declaration.hasAnnotation(Names.ExportDeclaration, session)
        )
            return null

        if (declaration.isLocalMember) {
            reporter.reportOn(
                declaration.source,
                Diagnostics.SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS
            )
            return true
        }

        if (declaration is FirMemberDeclaration) {
            val visibility = declaration.publishedApiEffectiveVisibility ?: declaration.effectiveVisibility

            if (!visibility.publicApi) {
                reporter.reportOn(
                    declaration.source,
                    Diagnostics.SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API,
                    declaration.effectiveVisibility.toString()
                )
                return true
            }
        }
        return false
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirDeclaration) {
        isIllegalUse(declaration)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun <T : FirDeclaration> checkIllegalUse(declaration: T): Boolean {
        val isDeclarationIllegal = isIllegalUse(declaration)
        return isDeclarationIllegal

    }

}