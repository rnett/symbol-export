package dev.rnett.symbolexport

import dev.rnett.symbolexport.reference.ExportReferences

/**
 * Allows children of this declaration to be exported.
 * Does not export the declaration itself.
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ChildrenExported

/**
 * Generates a symbol entry for the annotated target.
 * All parents of the target must be marked with either [ExportSymbol] or [ChildrenExported].
 *
 * Annotation classes may use [ExportAnnotation] instead of this annotation to also export their schema.
 *
 * [ExportReferences] may be used to export 3rd party symbols, but should be used with caution.
 *
 * @see ChildrenExported
 * @see ExportAnnotation
 * @see ExportReceivers
 * @see ExportReferences
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE_PARAMETER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportSymbol

/**
 * Applies [ExportSymbol] to this function's dispatch and extension receivers, if the appropriate flags are set.
 * Applies to both by default.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY,
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportReceivers(val dispatch: Boolean = true, val extension: Boolean = true)

/**
 * Implies [ExportSymbol], and also exports the annotation's properties in a way that allows annotations to be easily read or created by users of the symbols (e.g. to/from `FirAnnotation` or `IrAnnotation`).
 *
 * Requires any referenced annotation types to also use [ExportAnnotation].
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportAnnotation