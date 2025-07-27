package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.exporter.Helpers.createMemberName
import dev.rnett.symbolexport.fir.exporter.Helpers.getIndexStartOffset
import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.FirValueParameterKind
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol

class ValueParameterExporter(illegalUseChecker: IllegalUseCheckerImpl, session: FirSession) : BaseSymbolExporter<FirValueParameter>(illegalUseChecker, session) {
    override fun getParent(declaration: FirValueParameter): FirBasedSymbol<*>? =
        declaration.containingDeclarationSymbol

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirValueParameter): Pair<KtSourceElement?, InternalName>? {
        val parent = declaration.symbol.containingDeclarationSymbol as FirCallableSymbol<*>
        val parentName = createMemberName(parent)

        val type = when (declaration.valueParameterKind) {
            FirValueParameterKind.Regular -> VALUE
            FirValueParameterKind.ContextParameter -> CONTEXT
            FirValueParameterKind.LegacyContextReceiver -> {
                return null
            }
        }

        val indexInList = when (type) {
            VALUE -> {
                parent as FirFunctionSymbol<*>
                parent.valueParameterSymbols.indexOf(declaration.symbol)
            }

            CONTEXT -> parent.contextParameterSymbols.indexOf(declaration.symbol)
        }
        if (indexInList < 0) error("Could not find $declaration in $parent")

        return declaration.source to
                InternalName.IndexedParameter(
                    parentName,
                    declaration.name.asString(),
                    getIndexStartOffset(parent, type) + indexInList,
                    indexInList,
                    type
                )
    }
}