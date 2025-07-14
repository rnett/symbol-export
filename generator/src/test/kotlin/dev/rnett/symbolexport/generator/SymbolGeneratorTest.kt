package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalNameEntry
import dev.rnett.symbolexport.internal.ProjectCoordinates
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SymbolGeneratorTest {

    private val json = Json {
        isLenient = true
    }

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test writeSymbols with empty files list`() {
        val outputDir = File(tempDir, "output")
        val generator = SymbolGenerator(outputDir, "test.package", false)

        generator.generateSymbolsFile(emptyList())

        // No files should be created
        assertFalse(outputDir.exists())
    }

    @Test
    fun `test writeSymbols with flattenProjects true`() {
        val outputDir = File(tempDir, "output")
        val generator = SymbolGenerator(outputDir, "test.package", true)

        val inputFile = createInputFile()

        generator.generateSymbolsFile(listOf(inputFile))

        // Verify output file was created
        val outputFile = File(outputDir, "test/package/Symbols.kt")
        assertTrue(outputFile.exists())

        // Verify content
        val content = outputFile.readText()
        assertTrue(content.contains("internal object Symbols {"))
        assertTrue(content.contains("// Symbols from project `TestProject` with coordinates `com.example:test:1.0.0`"))
        assertTrue(content.contains("val dev_rnett_test_topLevelFunction: TopLevelMember"))
    }

    @Test
    fun `test writeSymbols with flattenProjects false`() {
        val outputDir = File(tempDir, "output")
        val generator = SymbolGenerator(outputDir, "test.package", false)

        val inputFile = createInputFile()

        generator.generateSymbolsFile(listOf(inputFile))

        // Verify output file was created
        val outputFile = File(outputDir, "test/package/Symbols.kt")
        assertTrue(outputFile.exists())

        // Verify content
        val content = outputFile.readText()
        assertTrue(content.contains("internal object Symbols {"))
        assertTrue(content.contains("// Symbols from project `TestProject` with coordinates `com.example:test:1.0.0`"))
        assertTrue(content.contains("object TestProject {"))
        assertTrue(content.contains("val dev_rnett_test_topLevelFunction: TopLevelMember"))
    }

    @Test
    fun `test writeSymbols with multiple projects`() {
        val outputDir = File(tempDir, "output")
        val generator = SymbolGenerator(outputDir, "test.package", false)

        val inputFile1 = createInputFile("TestProject1")
        val inputFile2 = createInputFile("TestProject2")

        generator.generateSymbolsFile(listOf(inputFile1, inputFile2))

        // Verify output file was created
        val outputFile = File(outputDir, "test/package/Symbols.kt")
        assertTrue(outputFile.exists())

        // Verify content
        val content = outputFile.readText()
        assertTrue(content.contains("internal object Symbols {"))
        assertTrue(content.contains("// Symbols from project `TestProject1` with coordinates `com.example:test:1.0.0`"))
        assertTrue(content.contains("object TestProject1 {"))
        assertTrue(content.contains("// Symbols from project `TestProject2` with coordinates `com.example:test:1.0.0`"))
        assertTrue(content.contains("object TestProject2 {"))
    }

    private fun createInputFile(projectName: String = "TestProject"): File {
        val entry = InternalNameEntry(
            projectName = projectName,
            projectCoordinates = ProjectCoordinates("com.example", "test", "1.0.0"),
            sourceSetName = "commonMain",
            name = InternalName.TopLevelMember(
                packageName = listOf("dev", "rnett", "test"),
                name = "topLevelFunction"
            )
        )

        val file = File(tempDir, "$projectName.json")
        file.writeText(json.encodeToString(entry))
        return file
    }
}