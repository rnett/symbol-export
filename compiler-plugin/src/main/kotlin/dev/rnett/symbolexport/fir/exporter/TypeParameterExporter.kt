package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.fir.IllegalUseCheckerImpl
import dev.rnett.symbolexport.fir.Names
import dev.rnett.symbolexport.fir.exporter.Helpers.createClassName
import dev.rnett.symbolexport.fir.exporter.Helpers.createMemberName
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol

class TypeParameterExporter(illegalUseChecker: IllegalUseCheckerImpl, session: FirSession) : BaseSymbolExporter<FirTypeParameter>(illegalUseChecker, session) {
    context(context: CheckerContext)
    override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirTypeParameter): Boolean {
        return hasExportAnnotation || declaration.hasAnnotation(Names.EXPORT_ANNOTATION_CLASSID, session)
    }

    override fun getParent(declaration: FirTypeParameter): FirBasedSymbol<*>? =
        declaration.containingDeclarationSymbol

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun createName(declaration: FirTypeParameter): Pair<KtSourceElement?, InternalName>? {
        val parent = declaration.symbol.containingDeclarationSymbol

        val parentName: InternalName
        val indexInList: Int

        if (parent is FirCallableSymbol<*>) {
            parentName = createMemberName(parent)
            indexInList = parent.typeParameterSymbols.indexOf(declaration.symbol)
        } else if (parent is FirClassSymbol<*>) {
            parentName = createClassName(parent)
            indexInList = parent.typeParameterSymbols.indexOf(declaration.symbol)
        } else {
            return null
        }

        if (indexInList < 0) error("Could not find $declaration in $parent")

        return declaration.source to InternalName.TypeParameter(
            parentName,
            declaration.name.asString(),
            indexInList
        )
    }
}