package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalSymbol
import org.junit.jupiter.api.Test

class CommonGeneratorTest : AbstractGeneratorSnapshotTest() {

    @Test
    fun testQNameClassifier() {
        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestClass"))
        assertCodeBlockSnapshot(CommonGenerator.qName(symbol), "common/qNameClassifier")
    }

    @Test
    fun testQNameTopLevelFunction() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), null, "testFunc", emptyList(), null)
        assertCodeBlockSnapshot(CommonGenerator.qName(symbol), "common/qNameTopLevelFunction")
    }

    @Test
    fun testQNameMemberFunction() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), listOf("TestClass"), "memberFunc", emptyList(), null)
        assertCodeBlockSnapshot(CommonGenerator.qName(symbol), "common/qNameMemberFunction")
    }

    @Test
    fun testQNameEnumEntry() {
        val owner = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestEnum"))
        val symbol = InternalSymbol.EnumEntry(owner, "ENTRY", 0)
        assertCodeBlockSnapshot(CommonGenerator.qName(symbol), "common/qNameEnumEntry")
    }

    @Test
    fun testSig() {
        val symbol = InternalSymbol.Function(
            listOf("com", "example"),
            null,
            "testFunc",
            listOf(
                InternalSymbol.ParameterSignature(
                    "p1",
                    false,
                    InternalSymbol.ParamType.ClassBased("kotlin.Int", false, emptyList())
                ),
                InternalSymbol.ParameterSignature(
                    "p2",
                    true,
                    InternalSymbol.ParamType.ClassBased("kotlin.String", true, emptyList())
                )
            ),
            null
        )
        assertCodeBlockSnapshot(CommonGenerator.sig(symbol), "common/sig")
    }

    @Test
    fun testComplexSig() {
        val symbol = InternalSymbol.Function(
            listOf("com", "example"),
            null,
            "testFunc",
            listOf(
                InternalSymbol.ParameterSignature(
                    "p1",
                    false,
                    InternalSymbol.ParamType.ClassBased(
                        "kotlin.collections.List",
                        false,
                        listOf(
                            InternalSymbol.ParamTypeArg.TypeProjection(
                                InternalSymbol.ParamTypeArg.Variance.OUT,
                                InternalSymbol.ParamType.ClassBased("kotlin.Number", false, emptyList())
                            )
                        )
                    )
                )
            ),
            null
        )
        assertCodeBlockSnapshot(CommonGenerator.sig(symbol), "common/sigComplex")
    }
}
