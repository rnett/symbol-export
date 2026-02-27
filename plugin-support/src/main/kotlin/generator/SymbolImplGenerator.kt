package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import dev.rnett.symbolexport.internal.InternalSymbol

/**
 * Configures the [TypeSpec] for symbols using [dev.rnett.symbolexport.symbol.v2.SymbolDeclaration].
 *
 * This generator adds the appropriate superclass and constructor parameters to the symbol object so that it implements `HasDeclaration<>` with the SymbolDeclaration corresponding to the given symbol.
 */
internal interface SymbolImplGenerator {
    fun addSymbolDeclarationInstance(builder: TypeSpec.Builder, symbol: InternalSymbol)

    companion object : SymbolImplGenerator {
        override fun addSymbolDeclarationInstance(builder: TypeSpec.Builder, symbol: InternalSymbol) {
            val isConstructor = symbol is InternalSymbol.Function && symbol.name == Names.INIT

            val declarationType = when (symbol) {
                is InternalSymbol.Classifier -> Names.ClassSymbolDeclaration
                is InternalSymbol.EnumEntry -> Names.EnumEntrySymbolDeclaration
                is InternalSymbol.Function -> if (isConstructor) {
                    Names.ConstructorSymbolDeclaration
                } else {
                    Names.SimpleFunctionSymbolDeclaration
                }

                is InternalSymbol.Property -> Names.PropertySymbolDeclaration
            }

            builder.superclass(Names.HasDeclaration.parameterizedBy(declarationType))
            builder.addSuperclassConstructorParameter("%T.Impl(%L)", declarationType, SymbolInstanceGenerator.symbolInstance(symbol))
        }
    }
}
