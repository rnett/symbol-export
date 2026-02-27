package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.TypeSpec
import dev.rnett.symbolexport.internal.InternalSymbol
import org.junit.jupiter.api.Test

class SymbolImplGeneratorTest : AbstractGeneratorSnapshotTest() {

    private fun testSymbolImpl(symbol: InternalSymbol, name: String) {
        val builder = TypeSpec.objectBuilder("Test")
        SymbolImplGenerator.addSymbolDeclarationInstance(builder, symbol)
        assertSnapshot(builder.build(), "symbolImpl/$name")
    }

    @Test
    fun testClassifierImpl() {
        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestClass"))
        testSymbolImpl(symbol, "classifier")
    }

    @Test
    fun testFunctionImpl() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), null, "testFunc", emptyList(), null)
        testSymbolImpl(symbol, "function")
    }

    @Test
    fun testConstructorImpl() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), listOf("TestClass"), Names.INIT, emptyList(), null)
        testSymbolImpl(symbol, "constructor")
    }

    @Test
    fun testPropertyImpl() {
        val symbol = InternalSymbol.Property(listOf("com", "example"), listOf("TestClass"), "testProp", null)
        testSymbolImpl(symbol, "property")
    }

    @Test
    fun testEnumEntryImpl() {
        val owner = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestEnum"))
        val symbol = InternalSymbol.EnumEntry(owner, "ENTRY", 0)
        testSymbolImpl(symbol, "enumEntry")
    }
}
