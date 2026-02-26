package dev.rnett.symbolexport.reference

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty


/**
 *  **Warning:** Relying on symbols you do not control is dangerous, as they may change in a way that makes backwards compatability impossible.
 *  Prefer creating bridge methods/types where possible.
 *
 *  Applied to an object that implements [BaseReferenceExporter] to trigger reference exporting according to the methods used.
 *  Checks for method calls in init blocks - method calls anywhere else is an error (except `placeholder`).
 *
 *  All implementations of [BaseReferenceExporter] must be objects annotated with this annotation.
 *
 *  @see BaseReferenceExporter
 */
@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.SOURCE)
public annotation class ExportReferences

/**
 * See [ExportReferences].
 */
@Suppress("unused")
public abstract class BaseReferenceExporter {
    /**
     * A non-exported placeholder.
     * Useful to resolve method overloads for use with [exportReference].
     * ```kotlin
     * mapOf(*placeholder<Array<Pair<String, String>>>())
     * ```
     *
     */
    protected fun <T> placeholder(): T {
        error("This is a placeholder that should never be called.")
    }

    /**
     * Exports the symbol [value] refers to.  [value] must be a function call (including constructors) or property access.
     *
     * Exports from the call of the expression its argument evaluates to.  For example:
     * ```kotlin
     * exportReferenced(listOf<Int>().toSet())
     * ```
     * will export `kotlin.collections.toSet`.
     *
     * @param includeParameters Whether to also export the parameters of functions. Must be a literal `true` or `false`.
     * @param includeTypeParameters Whether to also export the type parameters of functions or properties. Must be a literal `true` or `false`.
     */
    protected fun exportReferenced(
        value: Any?,
        includeParameters: Boolean = false,
        includeTypeParameters: Boolean = false
    ) {
        error("This is a placeholder that should never be called.")
    }


    /**
     * Export a reference to the passed function. [reference] must be a literal `::` expression.
     *
     * If the function has overloads, use [exportReferenced] instead.
     *
     * @param includeParameters Whether to also export the parameters of functions. Must be a literal `true` or `false`.
     * @param includeTypeParameters Whether to also export the type parameters of functions or properties. Must be a literal `true` or `false`.
     */
    protected fun exportReference(
        reference: KFunction<*>,
        includeParameters: Boolean = false,
        includeTypeParameters: Boolean = false
    ) {
        error("This is a placeholder that should never be called.")
    }

    /**
     * Export a reference to the passed property. [reference] must be a literal `::` expression.
     *
     * If the property has overloads, use [exportReferenced] instead.
     *
     * @param includeParameters Whether to also export the parameters of functions. Must be a literal `true` or `false`.
     * @param includeTypeParameters Whether to also export the type parameters of functions or properties. Must be a literal `true` or `false`.
     */
    protected fun exportReference(
        reference: KProperty<*>,
        includeParameters: Boolean = false,
        includeTypeParameters: Boolean = false
    ) {
        error("This is a placeholder that should never be called.")
    }

    /**
     * Export a reference to the passed class. [clazz] must be a class literal.
     *
     * @param includeTypeParameters Whether to also export the type parameters of functions or properties. Must be a literal `true` or `false`.
     */
    protected fun exportClass(clazz: KClass<*>, includeTypeParameters: Boolean = false) {
        error("This is a placeholder that should never be called.")
    }

    /**
     * Exports all entries of [enumClass], and [enumClass] itself.  [enumClass] must be a class literal.
     */
    protected fun exportEnumEntries(enumClass: KClass<out Enum<*>>) {
        error("This is a placeholder that should never be called.")
    }

    /**
     * Exports [annotation] as if it were annotated with [ExportAnnotation].
     * [annotation] must be a class literal.
     */
    protected fun exportAnnotation(annotation: KClass<out Annotation>) {
        error("This is a placeholder that should never be called.")
    }
}