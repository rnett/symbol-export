package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.SymbolTarget
import dev.rnett.symbolexport.internal.InternalDeclaration
import dev.rnett.symbolexport.internal.InternalSymbol
import dev.rnett.symbolexport.postprocessor.TargetSymbol
import org.junit.jupiter.api.Test

class DeclarationGeneratorTest : AbstractGeneratorSnapshotTest() {

    @Test
    fun testSimpleSymbol() {
        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestClass"))
        val targetSymbol = TargetSymbol(symbol, null, setOf(SymbolTarget("jvm")))
        val builder = DeclarationGenerator.addDeclaration(targetSymbol, parentName)
        assertSnapshot(builder.build(), "declaration/simpleSymbol")
    }

    @Test
    fun testExportedName() {
        val symbol = InternalSymbol.Property(listOf("com", "example"), null, "prop", "exportedProp")
        val targetSymbol = TargetSymbol(symbol, null, setOf(SymbolTarget("jvm")))
        val builder = DeclarationGenerator.addDeclaration(targetSymbol, parentName)
        assertSnapshot(builder.build(), "declaration/exportedName")
    }

    @Test
    fun testWithDeclaration() {
        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestClass"))
        val declaration = InternalDeclaration.Classifier(
            symbol,
            emptyList(),
            null,
            null,
            true,
            false
        )
        val targetSymbol = TargetSymbol(symbol, declaration, setOf(SymbolTarget("jvm")))
        val builder = DeclarationGenerator.addDeclaration(targetSymbol, parentName)
        assertSnapshot(builder.build(), "declaration/withDeclaration")
    }
}
