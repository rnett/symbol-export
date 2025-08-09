package dev.rnett.lattice

import dev.rnett.lattice.tests.BaseDiagnosticCompilerTest
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(
            testDataRoot = "src/testData",
            testsRoot = "src/test-gen",
        ) {
            testClass<BaseDiagnosticCompilerTest>("ExportTestGenerated") {
                model("export")
            }
            testClass<BaseDiagnosticCompilerTest>("ErrorsTestGenerated") {
                model("errors")
            }
        }
    }
}
