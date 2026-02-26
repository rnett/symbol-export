package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.CodeBlock
import dev.rnett.symbolexport.internal.InternalSymbol

/**
 * Generates [com.squareup.kotlinpoet.CodeBlock]s for instances of symbols (like `ClassSymbol`, `SimpleFunctionSymbol`, etc.).
 */
internal object SymbolInstanceGenerator {
    fun symbolInstance(symbol: InternalSymbol): CodeBlock {
        val isConstructor = symbol is InternalSymbol.Function && symbol.name == Names.INIT
        val symbolType = when (symbol) {
            is InternalSymbol.Classifier -> Names.ClassSymbol
            is InternalSymbol.EnumEntry -> Names.EnumEntrySymbol
            is InternalSymbol.Function -> if (isConstructor) {
                Names.ConstructorSymbol
            } else {
                Names.SimpleFunctionSymbol
            }

            is InternalSymbol.Property -> Names.PropertySymbol
        }

        val qName = CommonGenerator.qName(symbol)
        return if (symbol is InternalSymbol.Function) {
            val sig = CommonGenerator.sig(symbol)
            CodeBlock.of("%T(%L, %L)", symbolType, qName, sig)
        } else {
            CodeBlock.of("%T(%L)", symbolType, qName)
        }
    }
}
