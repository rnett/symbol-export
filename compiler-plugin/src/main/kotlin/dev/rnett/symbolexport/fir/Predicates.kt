package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.fir.Names.EXPORT_ANNOTATION_ANNOTATION_FQN
import dev.rnett.symbolexport.fir.Names.EXPORT_ANNOTATION_FQN
import dev.rnett.symbolexport.fir.Names.EXPORT_REFERENCES_ANNOTATION_FQN
import dev.rnett.symbolexport.fir.Names.PARENT_ANNOTATION_FQN
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate

object Predicates {

    val export = DeclarationPredicate.create {
        annotated(EXPORT_ANNOTATION_FQN)
    }

    val childrenExported = DeclarationPredicate.create {
        annotated(PARENT_ANNOTATION_FQN)
    }

    val annotationExport = DeclarationPredicate.create {
        annotated(EXPORT_ANNOTATION_ANNOTATION_FQN)
    }

    val parentAnnotationExport = DeclarationPredicate.create {
        parentAnnotated(EXPORT_ANNOTATION_ANNOTATION_FQN)
    }

    val exportReferences = DeclarationPredicate.create {
        annotated(EXPORT_REFERENCES_ANNOTATION_FQN)
    }

    val ancestorExportsReferences = DeclarationPredicate.create {
        ancestorAnnotated(EXPORT_REFERENCES_ANNOTATION_FQN)
    }
}