package dev.rnett.test

import dev.rnett.symbolexport.ChildrenExported
import dev.rnett.symbolexport.ExportSymbol


@ExportSymbol
public fun topLevelFunction() = 3

fun notExposedFun() = 1

@ExportSymbol
val topLevelProperty = 3

val notExposedProperty = 1

@ExportSymbol
class ExposedClass(@property:ExportSymbol val prop: Int) {
    @ChildrenExported
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

@ChildrenExported
enum class TestEnum {
    @ExportSymbol
    A,
    B;
}
