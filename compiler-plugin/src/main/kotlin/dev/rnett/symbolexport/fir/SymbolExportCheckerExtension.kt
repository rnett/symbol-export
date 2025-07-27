package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.NameReporter
import dev.rnett.symbolexport.fir.exporter.ClassExporter
import dev.rnett.symbolexport.fir.exporter.DispatchReceiverExporter
import dev.rnett.symbolexport.fir.exporter.EnumEntryExporter
import dev.rnett.symbolexport.fir.exporter.ExtensionReceiverExporter
import dev.rnett.symbolexport.fir.exporter.MemberExporter
import dev.rnett.symbolexport.fir.exporter.MemberExtensionReceiverExporter
import dev.rnett.symbolexport.fir.exporter.SymbolExporter
import dev.rnett.symbolexport.fir.exporter.TypeParameterExporter
import dev.rnett.symbolexport.fir.exporter.ValueParameterExporter
import dev.rnett.symbolexport.fir.exporter.exporterCheckersOf
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar

class SymbolExportCheckerExtension(session: FirSession, val warnOnExported: Boolean, val nameReporter: NameReporter) :
    FirAdditionalCheckersExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(
            Predicates.exportPredicate,
            Predicates.childrenExportedPredicate,
            Predicates.annotatedWithExport
        )
    }

    fun <T : FirDeclaration> exportCheckersOf(vararg checkers: SymbolExporter<T>) = exporterCheckersOf(
        nameReporter,
        warnOnExported,
        *checkers
    )

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        private val illegalUseChecker = IllegalUseCheckerImpl(session)
        private val extensionReceiverChecker = ExtensionReceiverExporter(illegalUseChecker, session)

        override val basicDeclarationCheckers = setOf(
            illegalUseChecker
        )

        override val classCheckers = exportCheckersOf(
            ClassExporter(illegalUseChecker, session)
        )
        override val callableDeclarationCheckers = exportCheckersOf(
            MemberExporter(illegalUseChecker, session),
            DispatchReceiverExporter(illegalUseChecker, session),
            MemberExtensionReceiverExporter(extensionReceiverChecker, session)
        )

        override val valueParameterCheckers = exportCheckersOf(
            ValueParameterExporter(illegalUseChecker, session)
        )
        override val typeParameterCheckers = exportCheckersOf(
            TypeParameterExporter(illegalUseChecker, session)
        )
        override val enumEntryCheckers = exportCheckersOf(
            EnumEntryExporter(illegalUseChecker, session)
        )
        //TODO this never gets called. Switch to it once it works
//        override val receiverParameterCheckers: Set<FirReceiverParameterChecker> = setOf(ExtensionReceiverChecker())
    }
}
