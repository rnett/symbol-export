package dev.rnett.symbolexport.symbol.compiler.annotation.ir

import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.compiler.annotation.fir.enum
import dev.rnett.symbolexport.symbol.compiler.annotation.fir.kClass
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.classIdOrFail


@UnsafeDuringIrConstructionAPI
public fun AnnotationArgument.Companion.kClass(symbol: IrClassSymbol): AnnotationArgument.KClass = AnnotationArgument.kClass(symbol.owner.classIdOrFail)

@UnsafeDuringIrConstructionAPI
public fun AnnotationArgument.Companion.enum(symbol: IrClassSymbol, name: String): AnnotationArgument.EnumEntry =
    AnnotationArgument.enum(symbol.owner.classIdOrFail, name)
