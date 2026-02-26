package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.TypeSpec
import dev.rnett.symbolexport.internal.InternalDeclaration

/**
 * Configures the [TypeSpec] for symbols using [dev.rnett.symbolexport.symbol.v2.Declaration].
 *
 * This generator adds the appropriate superclass and constructor parameters to the symbol object so that it implements `HasDeclaration<>` with the Declaration corresponding to the given symbol.
 * To fully represent the declaration, it likely will need to generate an object with parameters, type params, etc.
 */
internal object DeclarationImplGenerator {
    fun addDeclarationInstance(builder: TypeSpec.Builder, declaration: InternalDeclaration) {
        TODO("stub")
    }
}
