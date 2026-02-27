package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalSymbol
import org.junit.jupiter.api.Test

class SymbolInstanceGeneratorTest : AbstractGeneratorSnapshotTest() {

    @Test
    fun testClassifierInstance() {
        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestClass"))
        assertCodeBlockSnapshot(SymbolInstanceGenerator.symbolInstance(symbol), "instance/classifier")
    }

    @Test
    fun testTopLevelFunctionInstance() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), null, "testFunc", emptyList(), null)
        assertCodeBlockSnapshot(SymbolInstanceGenerator.symbolInstance(symbol), "instance/topLevelFunction")
    }

    @Test
    fun testMemberFunctionInstance() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), listOf("TestClass"), "memberFunc", emptyList(), null)
        assertCodeBlockSnapshot(SymbolInstanceGenerator.symbolInstance(symbol), "instance/memberFunction")
    }

    @Test
    fun testConstructorInstance() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), listOf("TestClass"), Names.INIT, emptyList(), null)
        assertCodeBlockSnapshot(SymbolInstanceGenerator.symbolInstance(symbol), "instance/constructor")
    }

    @Test
    fun testPropertyInstance() {
        val symbol = InternalSymbol.Property(listOf("com", "example"), listOf("TestClass"), "testProp", null)
        assertCodeBlockSnapshot(SymbolInstanceGenerator.symbolInstance(symbol), "instance/property")
    }

    @Test
    fun testEnumEntryInstance() {
        val owner = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestEnum"))
        val symbol = InternalSymbol.EnumEntry(owner, "ENTRY", 0)
        assertCodeBlockSnapshot(SymbolInstanceGenerator.symbolInstance(symbol), "instance/enumEntry")
    }
}
