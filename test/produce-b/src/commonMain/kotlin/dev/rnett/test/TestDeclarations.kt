package dev.rnett.test

import dev.rnett.symbolexport.ExportSymbol


@ExportSymbol
fun topLevelFunction() = 3

fun notExposedFun() = 1

@ExportSymbol
val topLevelProperty = 3

val notExposedProperty = 1

@ExportSymbol
class ProducerBOnly {
    @ExportSymbol
    fun test() = 3
}