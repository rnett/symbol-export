package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.Names
import dev.rnett.symbolexport.Names.EXPORT_ANNOTATION_ANNOTATION_FQN
import dev.rnett.symbolexport.Names.EXPORT_REFERENCES_ANNOTATION_FQN
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate

object Predicates {

    val export = DeclarationPredicate.create {
        annotated(Names.ExportSymbol.asSingleFqName())
    }

    val childrenExported = DeclarationPredicate.create {
        annotated(Names.ChildrenExported.asSingleFqName())
    }

    val declarationExport = DeclarationPredicate.create {
        annotated(Names.ExportDeclaration.asSingleFqName())
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

    val exportParameters = DeclarationPredicate.create {
        annotated(Names.ExportParameters.asSingleFqName())
    }
}