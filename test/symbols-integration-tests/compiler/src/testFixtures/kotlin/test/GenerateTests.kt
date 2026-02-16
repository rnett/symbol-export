package test

import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5
import test.cases.Cases
import test.tests.BaseCompilerTest
import java.nio.file.StandardOpenOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText

@OptIn(ExperimentalPathApi::class)
fun main() {

    val testDataRoot = "src/testData"

    val caseDir = Path(testDataRoot).resolve("cases")

    if (caseDir.exists())
        caseDir.listDirectoryEntries("*.kt").forEach {
            it.deleteIfExists()
        }

    caseDir.createDirectories()

    Cases.cases.forEach {
        val source = buildString {
            appendLine("// GENERATED FROM ${Cases::class}, DO NOT EDIT")
            appendLine()
            appendLine(it.source.trimIndent().trim())
            appendLine()

            if ("fun box(" !in it.source) {
                appendLine("fun box() = \"OK\"")
                appendLine()
            }
        }

        caseDir.resolve(it.sourceFileName).writeText(source, options = arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE))
    }

    generateTestGroupSuiteWithJUnit5 {
        testGroup(
            testDataRoot = testDataRoot,
            testsRoot = "src/test-gen",
        ) {
            testClass<BaseCompilerTest>("CasesTestGenerated") {
                model("cases")
            }
        }
    }
}
