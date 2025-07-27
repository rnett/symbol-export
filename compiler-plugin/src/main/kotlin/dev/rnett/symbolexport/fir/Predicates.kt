package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.fir.Names.EXPORT_ANNOTATION_FQN
import dev.rnett.symbolexport.fir.Names.PARENT_ANNOTATION_FQN
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate

object Predicates {

    val exportPredicate = DeclarationPredicate.create {
        annotated(EXPORT_ANNOTATION_FQN)
    }

    val childrenExportedPredicate = DeclarationPredicate.create {
        annotated(PARENT_ANNOTATION_FQN)
    }
    val annotatedWithExport = DeclarationPredicate.create {
        annotated(EXPORT_ANNOTATION_FQN)
    }
}