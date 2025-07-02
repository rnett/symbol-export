package dev.rnett.symbolexport


/**
 * Allows children of this declaration to be exported.
 * Does not export the declaration itself.
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ChildrenExported

/**
 * Generates a symbol entry for the annotated target.
 * All parents of the target must be marked with either [ExportSymbol] or [ChildrenExported].
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportSymbol