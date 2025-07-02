package dev.rnett.test

import dev.rnett.symbolexport.symbol.NameSegments
import dev.rnett.symbolexport.symbol.Symbol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SymbolsATest {

    @Test
    fun testExportedSymbols() {
        // Test that all exported symbols from TestDeclarations are in symbolsA.ALL_SYMBOLS
        // and that each symbol's value matches what it should be for that declaration

        // Common symbols - Check both presence in ALL_SYMBOLS and correct value
        // Top-level function
        assertTrue(Symbols.`produce-a`.dev_rnett_test_topLevelFunction in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelFunction"),
            Symbols.`produce-a`.dev_rnett_test_topLevelFunction
        )

        // Top-level property
        assertTrue(Symbols.`produce-a`.dev_rnett_test_topLevelProperty in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelProperty"),
            Symbols.`produce-a`.dev_rnett_test_topLevelProperty
        )

        // Class
        assertTrue(Symbols.`produce-a`.dev_rnett_test_ExposedClass in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass")
            ),
            Symbols.`produce-a`.dev_rnett_test_ExposedClass
        )

        // Class property
        assertTrue(Symbols.`produce-a`.dev_rnett_test_ExposedClass_prop in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass")
                ),
                name = "prop"
            ),
            Symbols.`produce-a`.dev_rnett_test_ExposedClass_prop
        )

        // Companion function
        assertTrue(Symbols.`produce-a`.dev_rnett_test_ExposedClass_Companion_exposedFun in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "Companion")
                ),
                name = "exposedFun"
            ),
            Symbols.`produce-a`.dev_rnett_test_ExposedClass_Companion_exposedFun
        )

        // Nested class
        assertTrue(Symbols.`produce-a`.dev_rnett_test_ExposedClass_ExposedNestedClass in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            Symbols.`produce-a`.dev_rnett_test_ExposedClass_ExposedNestedClass
        )

        // Nested class function
        assertTrue(Symbols.`produce-a`.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedFun in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "ExposedNestedClass")
                ),
                name = "exposedFun"
            ),
            Symbols.`produce-a`.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedFun
        )

        // Nested class property
        assertTrue(Symbols.`produce-a`.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedProperty in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "ExposedNestedClass")
                ),
                name = "exposedProperty"
            ),
            Symbols.`produce-a`.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedProperty
        )

        // JVM-specific symbol
        assertTrue(Symbols.`produce-a`.JvmMain.dev_rnett_test_jvmOnly in Symbols.`produce-a`.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "jvmOnly"),
            Symbols.`produce-a`.JvmMain.dev_rnett_test_jvmOnly
        )
    }

    @Test
    fun testNonExportedSymbols() {
        // Test that non-exported symbols from TestDeclarations are not in symbolsA.ALL_SYMBOLS

        // Check non-exported top-level function
        val notExposedFun = Symbol.TopLevelMember(
            packageName = NameSegments("dev", "rnett", "test"),
            name = "notExposedFun"
        )
        assertFalse(notExposedFun in Symbols.`produce-a`.ALL_SYMBOLS)

        // Check non-exported top-level property
        val notExposedProperty = Symbol.TopLevelMember(
            packageName = NameSegments("dev", "rnett", "test"),
            name = "notExposedProperty"
        )
        assertFalse(notExposedProperty in Symbols.`produce-a`.ALL_SYMBOLS)

        // Check non-exported nested class method
        val notExposedMethod = Symbol.ClassifierMember(
            classifier = Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            name = "notExposed"
        )
        assertFalse(notExposedMethod in Symbols.`produce-a`.ALL_SYMBOLS)

        // Check non-exported nested class property
        val notExposedNestedProperty = Symbol.ClassifierMember(
            classifier = Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            name = "notExposedProperty"
        )
        assertFalse(notExposedNestedProperty in Symbols.`produce-a`.ALL_SYMBOLS)
    }
}