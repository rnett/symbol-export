package test

import dev.rnett.symbolexport.symbol.annotation.asAnnotationArgument
import dev.rnett.symbolexport.symbol.kotlinpoet.annotation.toAnnotationSpec
import dev.rnett.test.Symbols
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinpoetTest {
    private val baseInstance = Symbols.`test-symbols`.test_symbols_TestAnnotation(
        stringProperty = "test",
        intProperty = 3,
        enumProperty = Symbols.`test-symbols`.test_symbols_TestEnum_A.asAnnotationArgument(),
        classProperty = Symbols.`test-symbols`.test_symbols_FooClass.asAnnotationArgument(),
        arrayProperty = listOf("test", "test2"),
        annotationProperty = Symbols.`test-symbols`.test_symbols_TestAnnotation_TestChildAnnotation(
            test = "test-child",
            cls = Symbols.`test-symbols`.test_symbols_BarClass.asAnnotationArgument()
        ),
        annotationArrayProperty = listOf(
            Symbols.`test-symbols`.test_symbols_TestAnnotation_TestChildAnnotation(
                test = "test-child-2",
                cls = Symbols.`test-symbols`.test_symbols_BarClass.asAnnotationArgument()
            ),
            Symbols.`test-symbols`.test_symbols_TestAnnotation_TestChildAnnotation(
                test = "test-child-3",
                cls = Symbols.`test-symbols`.test_symbols_BarClass.asAnnotationArgument()
            )
        ),
    )

    @Test
    fun canWriteToAnnotationSpec() {
        val spec = assertDoesNotThrow { baseInstance.toAnnotationSpec() }
        assertEquals("test.symbols.TestAnnotation", spec.typeName.toString())
    }

    @Test
    fun writesExpectedAnnotationSpec() {
        val spec = baseInstance.toAnnotationSpec()
        assertEquals(
            // language=kotlin
            """
                @test.symbols.TestAnnotation(stringProperty = "test", intProperty = 3, enumProperty = test.symbols.TestEnum.A, classProperty = test.symbols.FooClass::class, arrayProperty = ["test", "test2"], annotationProperty = test.symbols.TestAnnotation.TestChildAnnotation(test = "test-child", cls = test.symbols.BarClass::class), annotationArrayProperty = [test.symbols.TestAnnotation.TestChildAnnotation(test = "test-child-2", cls = test.symbols.BarClass::class), test.symbols.TestAnnotation.TestChildAnnotation(test = "test-child-3", cls = test.symbols.BarClass::class)])
            """.trimIndent(),
            spec.toString()
        )
    }

}