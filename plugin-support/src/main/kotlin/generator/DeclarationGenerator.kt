package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import dev.rnett.symbolexport.postprocessor.TargetSymbol

/**
 * Responsible for generating the [TypeSpec] for a single symbol.
 *
 * It determines whether the symbol should use a [dev.rnett.symbolexport.symbol.v2.Declaration] (via [DeclarationImplGenerator])
 * or a [dev.rnett.symbolexport.symbol.v2.SymbolDeclaration] (via [SymbolImplGenerator]).
 * Both are used for modern symbol export; the choice depends on whether `ExportDeclaration` or `ExportSymbol` was used.
 */
internal interface DeclarationGenerator {
    fun addDeclaration(targetSymbol: TargetSymbol, parentName: ClassName): TypeSpec.Builder

    companion object : DeclarationGenerator {
        override fun addDeclaration(targetSymbol: TargetSymbol, parentName: ClassName): TypeSpec.Builder {
            val ownSymbol = targetSymbol.symbol
            val name = ownSymbol.exportedName ?: ownSymbol.qualifiedNameSegments.last()

            val obj = TypeSpec.objectBuilder(name)

            if (targetSymbol.declaration != null) {
                DeclarationImplGenerator.addDeclarationInstance(obj, parentName.nestedClass(name), targetSymbol.declaration)
            } else {
                SymbolImplGenerator.addSymbolDeclarationInstance(obj, ownSymbol)
            }

            return obj
        }
    }
}
