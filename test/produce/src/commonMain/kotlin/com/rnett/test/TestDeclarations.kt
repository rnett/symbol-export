package com.rnett.test

import com.rnett.symbolexport.ExportSymbol


@ExportSymbol
fun topLevelFunction() = 3

fun notExposedFun() = 1

@ExportSymbol
val topLevelProperty = 3

val notExposedProperty = 1

@ExportSymbol
class ExposedClass(@ExportSymbol val prop: Int) {
    @ExportSymbol
    companion object {
        @ExportSymbol
        fun exposedFun() = 3
    }

    @ExportSymbol
    class ExposedNestedClass {
        fun notExposed() = 3

        @ExportSymbol
        fun exposedFun() = 3

        val notExposedProperty = 3

        @ExportSymbol
        val exposedProperty = 3
    }
}

val test = ExposedClass.exposedFun()