package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.rnett.symbolexport.SymbolTarget
import dev.rnett.symbolexport.postprocessor.ExportedSymbols
import dev.rnett.symbolexport.postprocessor.TargetSymbol
import dev.rnett.symbolexport.postprocessor.TargetSymbolTree
import java.nio.file.Path

/**
 * The main generator logic that orchestrates the creation of the exported symbols objects.
 *
 * It traverses the [TargetSymbolTree] and builds a hierarchy of Kotlin objects representing the exported symbols.
 * For each symbol, it uses [DeclarationGenerator] to create the appropriate [TypeSpec].
 */
internal object Generator {
    fun generateImport(basePackage: String, packageRoot: Path, importName: String, symbols: ExportedSymbols.V1) {
        val root = symbols.symbols.targets[SymbolTarget.all]!!.removePrefix()

        val symbolsObject = TypeSpec.objectBuilder(importName)
        generate(symbolsObject, root)
        val built = symbolsObject.build()

        FileSpec.builder(basePackage, importName)
            .addType(built)
            .build()
            .writeTo(packageRoot)
    }

    private fun generate(parent: TypeSpec.Builder, symbols: TargetSymbolTree) {
        symbols.children.forEach { (childName, symbols) ->
            val childObject = symbols.ownSymbol?.let { addSymbol(parent, it) } ?: TypeSpec.objectBuilder(childName)
            generate(childObject, symbols)
            parent.addType(childObject.build())
        }
    }

    fun addSymbol(parent: TypeSpec.Builder, it: TargetSymbol): TypeSpec.Builder {
        return DeclarationGenerator.addDeclaration(it)
    }
}

private fun TargetSymbolTree.removePrefix(): TargetSymbolTree {
    var current = this
    while (current.children.size == 1) {
        current = current.children.values.single()
    }
    return current
}

internal abstract class Test<T>(val value: T)