package dev.rnett.test

import dev.rnett.symbolexport.ChildrenExported
import dev.rnett.symbolexport.ExportAnnotation
import dev.rnett.symbolexport.ExportReceivers
import dev.rnett.symbolexport.ExportSymbol
import kotlin.reflect.KClass


@ExportSymbol
fun topLevelFunction() = 3

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

    @ExportSymbol
    constructor(@ExportSymbol param: String) : this(3)

    @ChildrenExported
    fun <@ExportSymbol T> withTypeParameters(t: T) = 3

    @ChildrenExported
    fun withValueParameters(@ExportSymbol t: Int) = 3

    @ChildrenExported
    fun @receiver:ExportSymbol Int.withExtensionReceiver() = 3

    @ChildrenExported
    @ExportReceivers
    fun Int.withExtensionReceiverAll() = 3

    @ChildrenExported
    context(@ExportSymbol a: Int)
    fun withContextParameters(): Int = 3

    @ChildrenExported
    @ExportReceivers(dispatch = false)
    fun Int.withExtensionReceiverJustExtension() = 3

    @ChildrenExported
    @ExportReceivers(extension = false)
    fun Int.withExtensionReceiverJustDispatch() = 3

    @ChildrenExported
    @ExportReceivers()
    fun withDispatch() = 5
}

@ChildrenExported
class WithValueParams<@ExportSymbol T>()

@ChildrenExported
enum class TestEnum {
    @ExportSymbol
    A,
    B;
}

@ExportAnnotation
annotation class TestAnnotation(
    val value: String,
    val other: Int = 3,
    val enu: TestEnum = TestEnum.A,
    val arr: Array<String> = [],
    val child: TestChildAnnotation
) {

    @ExportAnnotation
    annotation class TestChildAnnotation(val test: String, val cls: KClass<*>)
}