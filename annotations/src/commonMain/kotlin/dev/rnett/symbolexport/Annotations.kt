package dev.rnett.symbolexport


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