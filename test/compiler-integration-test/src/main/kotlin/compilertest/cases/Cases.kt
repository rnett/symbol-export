package compilertest.cases

import dev.rnett.symbolexport.symbol.annotation.asAnnotationArgument
import dev.rnett.symbolexport.symbol.compiler.annotation.fir.findAnnotation
import dev.rnett.symbolexport.symbol.compiler.annotation.fir.readAnnotation
import dev.rnett.symbolexport.symbol.compiler.annotation.fir.toFirAnnotation
import dev.rnett.symbolexport.symbol.compiler.annotation.ir.findAnnotation
import dev.rnett.symbolexport.symbol.compiler.annotation.ir.readAnnotation
import dev.rnett.symbolexport.symbol.compiler.annotation.ir.toIrAnnotation
import dev.rnett.test.Symbols
import kotlin.test.assertEquals

private val baseInstance = Symbols.`test-symbols`.compilertest_symbols_TestAnnotation(
    stringProperty = "compilertest",
    intProperty = 3,
    enumProperty = Symbols.`test-symbols`.compilertest_symbols_TestEnum_A.asAnnotationArgument(),
    classProperty = Symbols.`test-symbols`.compilertest_symbols_FooClass.asAnnotationArgument(),
    arrayProperty = listOf("compilertest", "test2"),
    annotationProperty = Symbols.`test-symbols`.compilertest_symbols_TestAnnotation_TestChildAnnotation(
        test = "test-child",
        cls = Symbols.`test-symbols`.compilertest_symbols_BarClass.asAnnotationArgument()
    ),
    annotationArrayProperty = listOf(
        Symbols.`test-symbols`.compilertest_symbols_TestAnnotation_TestChildAnnotation(
            test = "test-child-2",
            cls = Symbols.`test-symbols`.compilertest_symbols_BarClass.asAnnotationArgument()
        ),
        Symbols.`test-symbols`.compilertest_symbols_TestAnnotation_TestChildAnnotation(
            test = "test-child-3",
            cls = Symbols.`test-symbols`.compilertest_symbols_BarClass.asAnnotationArgument()
        )
    ),
)

private val baseInstanceSource = SourceGenerator {
    //language=kotlin
    """
        @TestAnnotation(
            stringProperty = "compilertest",
            intProperty = 3,
            enumProperty = TestEnum.A,
            classProperty = FooClass::class,
            arrayProperty = ["compilertest", "test2"],
            annotationProperty = TestAnnotation.TestChildAnnotation(
                test = "test-child",
                cls = BarClass::class
            ),
            annotationArrayProperty = [
                TestAnnotation.TestChildAnnotation(
                    test = "test-child-2",
                    cls = BarClass::class
                ),
                TestAnnotation.TestChildAnnotation(
                    test = "test-child-3",
                    cls = BarClass::class
                )
            ]
        )
        class $it
        """.trimIndent()
}

object Cases : TestCaseContainer() {

    val firWriteReadEquality by case {
        firStatus {
            val written = baseInstance.toFirAnnotation(session, it.source)
            val read = written.readAnnotation(baseInstance.annotation, session)
            assertEquals(baseInstance, read)
        }
    }

    val irWriteReadEquality by case {
        ir {
            val written = baseInstance.toIrAnnotation(context)
            val read = written.readAnnotation(baseInstance.annotation)
        }
    }

    val firWrite by case {
        firStatus {
            it.replaceAnnotations(it.annotations + baseInstance.toFirAnnotation(session, it.source))
        }
        assertAnnotationPresent(baseInstance.annotation)
    }

    val irWrite by case {
        ir {
            it.annotations += baseInstance.toIrAnnotation(context)
        }
        assertAnnotationPresent(baseInstance.annotation)
    }

    val firWriteIrRead by case {
        firStatus {
            it.replaceAnnotations(it.annotations + baseInstance.toFirAnnotation(session, it.source))
        }
        ir {
            val instance = it.findAnnotation(baseInstance.annotation)
            assertEquals(baseInstance, instance)
        }
    }

    val firRead by case {
        setup(baseInstanceSource)
        includeCaseClass = false

        firChecker {
            val read = it.findAnnotation(baseInstance.annotation, session)
            assertEquals(baseInstance, read)
        }
    }

    val irRead by case {
        setup(baseInstanceSource)
        includeCaseClass = false

        ir {
            val read = it.findAnnotation(baseInstance.annotation)
            assertEquals(baseInstance, read)
        }
    }

}