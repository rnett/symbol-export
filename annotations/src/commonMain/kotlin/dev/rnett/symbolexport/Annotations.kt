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

//TODO forbid names used by Symbol

/**
 * Generates a symbol entry for the annotated target.
 * All parents of the target must be marked with either [ExportSymbol] or [ChildrenExported].
 *
 * This annotation exports just the symbol reference to this declaration.
 * See [ExportDeclaration] exports a more full data model about the target.
 *
 * [exportedName] is used to disambiguate conflicts in the generated symbol accessors, if necessary.
 * Only relevant for functions - ignored otherwise.
 *
 * @see ChildrenExported
 * @see ExportDeclaration
 * @see ExportReferences
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportSymbol(val exportedName: String = "")

/**
 * Generates a symbol entry for the annotated target.
 * All parents of the target must be marked with [ExportSymbol], [ExportDeclaration], or [ChildrenExported].
 *
 * Applies [ExportSymbol], and exports some additional information, depending on the target:
 *  * Type parameters and parameters are exported
 *  * If the target is an annotation, a type-safe annotation reader and writer are created
 *  * If the target target is a class, a type-safe type constructor is created
 *  * If the target is a callable (function or property), a type-safe call reader and creator is created
 *  * If the target is a property, any public-ABI getter or setter is exported (TODO: backing field and delegate)
 *  * If the target is an enum, all entries are exported
 *
 * [exportedName] is used to disambiguate conflicts in the generated declaration accessors, if necessary.
 * Only relevant for functions - ignored otherwise.
 *
 * @see ChildrenExported
 * @see ExportSymbol
 */
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportDeclaration(val exportedName: String = "")
