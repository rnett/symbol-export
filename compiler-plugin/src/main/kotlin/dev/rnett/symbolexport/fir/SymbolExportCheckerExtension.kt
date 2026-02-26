package dev.rnett.symbolexport.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar

class SymbolExportCheckerExtension(session: FirSession) :
    FirAdditionalCheckersExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(
            Predicates.export,
            Predicates.childrenExported,
            Predicates.declarationExport,
            Predicates.parentAnnotationExport,
            Predicates.exportReferences,
            Predicates.ancestorExportsReferences
        )
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        //TODO actually use
        private val illegalUseChecker = IllegalUseCheckerImpl(session)
    }
}
