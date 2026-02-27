package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.ClassName
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
internal interface Generator {
    fun generateFile(basePackage: String, importName: String, symbols: ExportedSymbols.V1): FileSpec
    fun generateImport(basePackage: String, packageRoot: Path, importName: String, symbols: ExportedSymbols.V1)
    fun addSymbol(parent: TypeSpec.Builder, it: TargetSymbol, parentName: ClassName): TypeSpec.Builder

    companion object : Generator {
        override fun generateFile(basePackage: String, importName: String, symbols: ExportedSymbols.V1): FileSpec {
            val (root, rootPath) = symbols.symbols.targets[SymbolTarget.all]!!.removePrefix()

            val rootPackage = (basePackage + "." + rootPath.joinToString(".")).trim('.')

            val symbolsObject = TypeSpec.objectBuilder(importName)
            val symbolsObjectName = ClassName(rootPackage, importName)
            generate(symbolsObject, root, symbolsObjectName)
            val built = symbolsObject.build()

            return FileSpec.builder(basePackage, importName)
                .addType(built)
                .build()
        }

        override fun generateImport(basePackage: String, packageRoot: Path, importName: String, symbols: ExportedSymbols.V1) {
            generateFile(basePackage, importName, symbols).writeTo(packageRoot)
        }

        private fun generate(parent: TypeSpec.Builder, symbols: TargetSymbolTree, parentName: ClassName) {
            symbols.children.forEach { (childName, symbols) ->
                val childObject = symbols.ownSymbol?.let { addSymbol(parent, it, parentName) } ?: TypeSpec.objectBuilder(childName)
                val built = childObject.build()
                generate(childObject, symbols, parentName.nestedClass(built.name!!))
                parent.addType(built)
            }
        }

        override fun addSymbol(parent: TypeSpec.Builder, it: TargetSymbol, parentName: ClassName): TypeSpec.Builder {
            return DeclarationGenerator.addDeclaration(it, parentName)
        }
    }
}

private fun TargetSymbolTree.removePrefix(): Pair<TargetSymbolTree, List<String>> {
    var current = this
    val currentPath = mutableListOf<String>()
    while (current.children.size == 1 && current.ownSymbol == null) {
        if (current.name.isNotEmpty())
            currentPath.add(current.name)
        current = current.children.values.single()
    }
    return current to currentPath
}

internal abstract class Test<T>(val value: T)