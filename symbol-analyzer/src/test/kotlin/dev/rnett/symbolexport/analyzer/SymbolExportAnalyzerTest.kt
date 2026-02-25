package dev.rnett.symbolexport.analyzer

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SymbolExportAnalyzerTest : AbstractSymbolAnalyzerTest() {

    @Test
    fun testExportClass() {
        val source = """
            package test
            import dev.rnett.symbolexport.ExportSymbol

            @ExportSymbol
            class MyClass
        """.trimIndent()

        val results = runAnalysis(source)
        assertEquals(1, results.size)
        val name = results[0].name
        assertTrue(name is InternalName.Classifier)
        assertEquals("test", name.packageName.joinToString("."))
        assertEquals("MyClass", name.classNames.joinToString("."))
    }

    @Test
    fun testExportFunction() {
        val source = """
            package test
            import dev.rnett.symbolexport.ExportSymbol

            @ExportSymbol
            fun myFunction() {}
        """.trimIndent()

        val results = runAnalysis(source)
        assertEquals(1, results.size)
        val name = results[0].name
        assertTrue(name is InternalName.TopLevelMember)
        assertEquals("test", name.packageName.joinToString("."))
        assertEquals("myFunction", name.name)
    }

    @Test
    fun testExportProperty() {
        val source = """
            package test
            import dev.rnett.symbolexport.ExportSymbol

            @ExportSymbol
            val myProperty = 1
        """.trimIndent()

        val results = runAnalysis(source)
        assertEquals(1, results.size)
        val name = results[0].name
        assertTrue(name is InternalName.TopLevelMember)
        assertEquals("test", name.packageName.joinToString("."))
        assertEquals("myProperty", name.name)
    }

    @Test
    fun testChildrenExported() {
        val source = """
            package test
            import dev.rnett.symbolexport.ExportSymbol
            import dev.rnett.symbolexport.ChildrenExported

            @ChildrenExported
            class MyClass {
                @ExportSymbol
                fun myMethod() {}
            }
        """.trimIndent()

        val results = runAnalysis(source)
        assertEquals(1, results.size)
        val name = results[0].name
        assertTrue(name is InternalName.ClassifierMember)
        assertEquals("test.MyClass", name.classifier.qualifiedName)
        assertEquals("myMethod", name.name)
    }

    @Test
    fun testExportParameter() {
        val source = """
            package test
            import dev.rnett.symbolexport.ExportSymbol

            @ExportSymbol
            fun myFunction(@ExportSymbol param: Int) {}
        """.trimIndent()

        val results = runAnalysis(source)
        assertEquals(2, results.size)
        val funName = results.find { it.name is InternalName.TopLevelMember }!!.name as InternalName.TopLevelMember
        val paramName = results.find { it.name is InternalName.IndexedParameter }!!.name as InternalName.IndexedParameter

        assertEquals("test.myFunction", funName.qualifiedName)
        assertEquals("test.myFunction.param", paramName.qualifiedName)
        assertEquals("param", paramName.name)
        assertEquals(0, paramName.index)
    }
}
