package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.NameReporter
import dev.rnett.symbolexport.fir.exporter.AnnotationExporter
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
import dev.rnett.symbolexport.fir.exporter.reference.ReferencesExporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar

class SymbolExportCheckerExtension(session: FirSession, val warnOnExported: Boolean, val nameReporter: NameReporter) :
    FirAdditionalCheckersExtension(session) {

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(
            Predicates.export,
            Predicates.childrenExported,
            Predicates.annotationExport,
            Predicates.parentAnnotationExport,
            Predicates.exportReferences,
            Predicates.ancestorExportsReferences
        )
    }

    fun <T : FirDeclaration> exportCheckersOf(vararg checkers: SymbolExporter<T>) = exporterCheckersOf(
        nameReporter,
        warnOnExported,
        *checkers
    )

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        private val illegalUseChecker = IllegalUseCheckerImpl(session)
        private val extensionReceiverChecker = ExtensionReceiverExporter(session, illegalUseChecker)

        override val basicDeclarationCheckers = setOf(
            illegalUseChecker
        )

        override val classCheckers = exportCheckersOf(
            ClassExporter(session, illegalUseChecker),
            AnnotationExporter(session, illegalUseChecker),
            ReferencesExporter(session)
        )
        override val callableDeclarationCheckers = exportCheckersOf(
            MemberExporter(session, illegalUseChecker),
            DispatchReceiverExporter(session, illegalUseChecker),
            MemberExtensionReceiverExporter(extensionReceiverChecker)
        )

        override val valueParameterCheckers = exportCheckersOf(
            ValueParameterExporter(session, illegalUseChecker)
        )
        override val typeParameterCheckers = exportCheckersOf(
            TypeParameterExporter(session, illegalUseChecker)
        )
        override val enumEntryCheckers = exportCheckersOf(
            EnumEntryExporter(session, illegalUseChecker)
        )
        //TODO this never gets called. Switch to it once it works
//        override val receiverParameterCheckers: Set<FirReceiverParameterChecker> = setOf(ExtensionReceiverChecker())
    }
}
