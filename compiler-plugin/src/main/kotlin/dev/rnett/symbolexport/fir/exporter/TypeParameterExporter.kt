package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.Names
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol

class TypeParameterExporter(session: FirSession, illegalUseChecker: IllegalUseCheckerImpl) : BaseSymbolExporter<FirTypeParameter>(session, illegalUseChecker) {
    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirTypeParameter): Boolean {
        return hasExportAnnotation || declaration.hasAnnotation(Names.EXPORT_ANNOTATION_CLASSID, session)
    }

    override fun getParent(declaration: FirTypeParameter): FirBasedSymbol<*>? =
        declaration.containingDeclarationSymbol

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirTypeParameter): Pair<KtSourceElement?, InternalName>? {
        return declaration.source to InternalNames.typeParameterName(declaration)
    }
}