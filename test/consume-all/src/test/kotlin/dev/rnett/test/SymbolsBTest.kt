package dev.rnett.test

import dev.rnett.symbolexport.symbol.NameSegments
import dev.rnett.symbolexport.symbol.Symbol
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SymbolsBTest {

    @Test
    fun testExportedSymbols() {
        // Test that all exported symbols from TestDeclarations are in symbolsA.ALL_SYMBOLS
        // and that each symbol's value matches what it should be for that declaration

        // Common symbols - Check both presence in ALL_SYMBOLS and correct value
        // Top-level function
        assertTrue(Symbols.`produce-b`.dev_rnett_test_topLevelFunction in Symbols.`produce-b`.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelFunction"),
            Symbols.`produce-b`.dev_rnett_test_topLevelFunction
        )

        // Top-level property
        assertTrue(Symbols.`produce-b`.dev_rnett_test_topLevelProperty in Symbols.`produce-b`.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelProperty"),
            Symbols.`produce-b`.dev_rnett_test_topLevelProperty
        )

        // Class
        assertTrue(Symbols.`produce-b`.dev_rnett_test_ProducerBOnly in Symbols.`produce-b`.ALL_SYMBOLS)
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ProducerBOnly")
            ),
            Symbols.`produce-b`.dev_rnett_test_ProducerBOnly
        )

        // Class method
        assertTrue(Symbols.`produce-b`.dev_rnett_test_ProducerBOnly_test in Symbols.`produce-b`.ALL_SYMBOLS)
        assertEquals(
            Symbol.ClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ProducerBOnly")
                ),
                name = "test"
            ),
            Symbols.`produce-b`.dev_rnett_test_ProducerBOnly_test
        )

        // JVM-specific symbol
        assertTrue(Symbols.`produce-b`.JvmMain.dev_rnett_test_jvmOnly in Symbols.`produce-b`.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "jvmOnly"),
            Symbols.`produce-b`.JvmMain.dev_rnett_test_jvmOnly
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
        assertFalse(notExposedFun in Symbols.`produce-b`.ALL_SYMBOLS)

        // Check non-exported top-level property
        val notExposedProperty = Symbol.TopLevelMember(
            packageName = NameSegments("dev", "rnett", "test"),
            name = "notExposedProperty"
        )
        assertFalse(notExposedProperty in Symbols.`produce-b`.ALL_SYMBOLS)
    }
}