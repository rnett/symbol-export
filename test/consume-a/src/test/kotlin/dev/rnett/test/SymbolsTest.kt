package dev.rnett.test

import dev.rnett.symbolexport.symbol.NameSegments
import dev.rnett.symbolexport.symbol.Symbol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SymbolsTest {

    @Test
    fun testExportedSymbols() {
        // Test that all exported symbols from TestDeclarations are in Symbols.ALL_SYMBOLS
        // and that each symbol's value matches what it should be for that declaration

        // Common symbols - Check both presence in ALL_SYMBOLS and correct value
        // Top-level function
        assertTrue(Symbols.dev_rnett_test_topLevelFunction in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelFunction"),
            Symbols.dev_rnett_test_topLevelFunction
        )

        // Top-level property
        assertTrue(Symbols.dev_rnett_test_topLevelProperty in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelProperty"),
            Symbols.dev_rnett_test_topLevelProperty
        )

        // Class
        assertTrue(Symbols.dev_rnett_test_ExposedClass in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass")
            ),
            Symbols.dev_rnett_test_ExposedClass
        )

        // Class property
        assertTrue(Symbols.dev_rnett_test_ExposedClass_prop in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass")
                ),
                name = "prop"
            ),
            Symbols.dev_rnett_test_ExposedClass_prop
        )

        // Companion function
        assertTrue(Symbols.dev_rnett_test_ExposedClass_Companion_exposedFun in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "Companion")
                ),
                name = "exposedFun"
            ),
            Symbols.dev_rnett_test_ExposedClass_Companion_exposedFun
        )

        // Nested class
        assertTrue(Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass
        )

        // Nested class function
        assertTrue(Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedFun in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "ExposedNestedClass")
                ),
                name = "exposedFun"
            ),
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedFun
        )

        // Nested class property
        assertTrue(Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedProperty in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "ExposedNestedClass")
                ),
                name = "exposedProperty"
            ),
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedProperty
        )

        // JVM-specific symbol
        assertTrue(Symbols.JvmMain.dev_rnett_test_jvmOnly in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "jvmOnly"),
            Symbols.JvmMain.dev_rnett_test_jvmOnly
        )
    }

    @Test
    fun testNonExportedSymbols() {
        // Test that non-exported symbols from TestDeclarations are not in Symbols.ALL_SYMBOLS

        // Check non-exported top-level function
        val notExposedFun = Symbol.TopLevelMember(
            packageName = NameSegments("dev", "rnett", "test"),
            name = "notExposedFun"
        )
        assertFalse(notExposedFun in Symbols.ALL_SYMBOLS)

        // Check non-exported top-level property
        val notExposedProperty = Symbol.TopLevelMember(
            packageName = NameSegments("dev", "rnett", "test"),
            name = "notExposedProperty"
        )
        assertFalse(notExposedProperty in Symbols.ALL_SYMBOLS)

        // Check non-exported nested class method
        val notExposedMethod = Symbol.ClassifierMember(
            classifier = Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            name = "notExposed"
        )
        assertFalse(notExposedMethod in Symbols.ALL_SYMBOLS)

        // Check non-exported nested class property
        val notExposedNestedProperty = Symbol.ClassifierMember(
            classifier = Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            name = "notExposedProperty"
        )
        assertFalse(notExposedNestedProperty in Symbols.ALL_SYMBOLS)
    }
}