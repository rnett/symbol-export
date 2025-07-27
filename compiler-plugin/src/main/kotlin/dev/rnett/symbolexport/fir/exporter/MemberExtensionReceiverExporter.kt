package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirCallableDeclaration

class MemberExtensionReceiverExporter<T : FirCallableDeclaration>(val extensionReceiverChecker: ExtensionReceiverExporter, session: FirSession) : SymbolExporter<T>(session) {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun exportSymbols(declaration: T): Iterable<Pair<KtSourceElement?, InternalName>> {
        return declaration.receiverParameter?.let { extensionReceiverChecker.exportSymbols(it) } ?: emptyList()
    }
}