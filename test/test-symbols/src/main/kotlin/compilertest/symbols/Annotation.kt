package compilertest.symbols

import dev.rnett.symbolexport.ExportAnnotation
import dev.rnett.symbolexport.ExportSymbol
import kotlin.reflect.KClass

@ExportSymbol
class FooClass

@ExportSymbol
class BarClass

@ExportSymbol
enum class TestEnum {
    @ExportSymbol
    A,

    @ExportSymbol
    B;
}

@ExportAnnotation
@Retention(AnnotationRetention.RUNTIME)
annotation class TestAnnotation(
    val stringProperty: String,
    val intProperty: Int,
    val enumProperty: TestEnum,
    val classProperty: KClass<*>,
    val arrayProperty: Array<String>,
    val annotationProperty: TestChildAnnotation,
    val annotationArrayProperty: Array<TestChildAnnotation>,
) {

    @ExportAnnotation
    annotation class TestChildAnnotation(val test: String, val cls: KClass<*>)
}