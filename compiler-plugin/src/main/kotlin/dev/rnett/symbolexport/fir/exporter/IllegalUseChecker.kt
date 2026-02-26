package dev.rnett.symbolexport.fir.exporter

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.declarations.FirDeclaration

interface IllegalUseChecker {
    context(context: CheckerContext, reporter: DiagnosticReporter)
    fun <T : FirDeclaration> checkIllegalUse(declaration: T): Boolean
}