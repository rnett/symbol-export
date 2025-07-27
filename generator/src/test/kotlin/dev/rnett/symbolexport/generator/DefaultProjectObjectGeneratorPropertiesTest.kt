package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProjectObjectGeneratorPropertiesTest {

    @Test
    fun testGenerateAllSymbolsProperty() {
        val fields = setOf(
            "Symbols.test_package_TestClass",
            "Symbols.test_package_TestClass_testMethod",
            "Symbols.test_package_testFunction"
        )

        val result = CodeFormatter.generateAllSymbolsProperty(fields)

        val expected = """
            val ALL_SYMBOLS: Set<Symbol> = setOf(
                Symbols.test_package_TestClass,
                Symbols.test_package_TestClass_testMethod,
                Symbols.test_package_testFunction,
            )
        """.trimIndent()

        assertEquals(expected, result.trimIndent())
    }

    @Test
    fun testGenerateAllSymbolsPropertyEmpty() {
        val fields = emptySet<String>()

        val result = CodeFormatter.generateAllSymbolsProperty(fields)

        val expected = """
            val ALL_SYMBOLS: Set<Symbol> = setOf(
            )
        """.trimIndent()

        assertEquals(expected, result.trimIndent())
    }

    @Test
    fun testGenerateProperties() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val classifierMember = InternalName.ClassifierMember(
            classifier = classifier,
            name = "testMethod"
        )

        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )

        val symbols = setOf(classifier, classifierMember, topLevelMember)

        val result = CodeFormatter.generateProperties(symbols)

        // Check that each symbol's property is generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))
        assertTrue(result.contains("public val `test_package_TestClass_testMethod`: ClassifierMember"))
        assertTrue(result.contains("public val `test_package_testFunction`: TopLevelMember"))

        // Check that each property has a javadoc comment
        assertTrue(result.contains("Generated from `test.package.TestClass`"))
        assertTrue(result.contains("Generated from `test.package.TestClass.testMethod`"))
        assertTrue(result.contains("Generated from `test.package.testFunction`"))
    }

    @Test
    fun testGeneratePropertiesEmpty() {
        val symbols = emptySet<InternalName>()

        val result = CodeFormatter.generateProperties(symbols)

        assertEquals("", result)
    }
}
