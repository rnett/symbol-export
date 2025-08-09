package test.cases

import dev.rnett.symbolexport.symbol.Symbol


class CaseBuilder {
    private val assertions = mutableListOf<SourceGenerator>()
    private val setup = mutableListOf<SourceGenerator>()
    private val firStatus = mutableListOf<FirTestCase>()
    private val firCheckers = mutableListOf<FirTestCase>()
    private val irCases = mutableListOf<IrTestCase>()

    var includeCaseClass: Boolean = true

    fun assert(assertion: SourceGenerator) {
        assertions += assertion
    }

    fun assert(assertion: String) = assert(SourceGenerator { assertion })

    fun setup(setup: SourceGenerator) {
        this.setup += setup
    }

    fun setup(setup: String) = setup(SourceGenerator { setup })

    fun firStatus(fir: FirTestCase) {
        firStatus += fir
    }

    fun firChecker(fir: FirTestCase) {
        firCheckers += fir
    }

    fun ir(ir: IrTestCase) {
        irCases += ir
    }

    fun build(caseName: String): TestCase {
        if (includeCaseClass)
            setup.add(0, SourceGenerator { "class $caseName" })
        return TestCase(
            caseName,
            Sources(setup, assertions).source(caseName),
            FirTestCase.combine(firStatus),
            FirTestCase.combine(firCheckers),
            IrTestCase.combine(irCases),
        )
    }
}

fun CaseBuilder.assertAnnotationPresent(annotation: Symbol.Annotation<*, *>) {
    assert { "assertTrue($it::class.java.isAnnotationPresent(${annotation.fullName.asString()}::class.java))" }
}

data class TestCase(
    val name: String,
    val source: String,
    val firStatus: FirTestCase,
    val firCheckers: FirTestCase,
    val ir: IrTestCase,
) {
    val sourceFileName get() = "${name.replace(" ", "_")}.kt"
}
