package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.SymbolTarget
import dev.rnett.symbolexport.internal.InternalSymbol
import dev.rnett.symbolexport.postprocessor.ExportedSymbols
import dev.rnett.symbolexport.postprocessor.SymbolTree
import dev.rnett.symbolexport.postprocessor.TargetSymbol
import dev.rnett.symbolexport.postprocessor.TargetSymbolTree
import org.junit.jupiter.api.Test

class GeneratorTest : AbstractGeneratorSnapshotTest() {

    @Test
    fun testGenerateFile() {
        val pkg = listOf("com", "example")
        val classSymbol = InternalSymbol.Classifier(pkg, listOf("TestClass"))
        val funcSymbol = InternalSymbol.Function(pkg, listOf("TestClass"), "func", emptyList(), null)

        val classNode = TargetSymbolTree(
            "TestClass",
            TargetSymbol(classSymbol, null, setOf(SymbolTarget("jvm"))),
            mapOf(
                "func" to TargetSymbolTree(
                    "func",
                    TargetSymbol(funcSymbol, null, setOf(SymbolTarget("jvm"))),
                    emptyMap()
                )
            )
        )

        val otherClassSymbol = InternalSymbol.Classifier(pkg, listOf("OtherClass"))
        val otherClassNode = TargetSymbolTree(
            "OtherClass",
            TargetSymbol(otherClassSymbol, null, setOf(SymbolTarget("jvm"))),
            emptyMap()
        )

        val root = TargetSymbolTree(
            "", null, mapOf(
                "com" to TargetSymbolTree(
                    "com", null, mapOf(
                        "example" to TargetSymbolTree(
                            "example", null, mapOf(
                                "TestClass" to classNode,
                                "OtherClass" to otherClassNode
                            )
                        )
                    )
                )
            )
        )

        val exportedSymbols = ExportedSymbols.V1(
            "TestProject",
            SymbolTree(mapOf(SymbolTarget.all to root))
        )

        val fileSpec = Generator.generateFile("com.example.generated", "Symbols", exportedSymbols)
        assertSnapshot(fileSpec, "generator/fullFile")
    }
}
