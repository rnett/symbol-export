package com.rnett.symbolexport

/**
 * Generates a name entry for the annotated target.
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportSymbol