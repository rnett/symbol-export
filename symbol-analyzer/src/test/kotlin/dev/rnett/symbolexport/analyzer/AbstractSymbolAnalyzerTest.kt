package dev.rnett.symbolexport.analyzer

import dev.rnett.symbolexport.internal.AnalysisArguments
import dev.rnett.symbolexport.internal.InternalNameEntry
import dev.rnett.symbolexport.internal.ProjectCoordinates
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

abstract class AbstractSymbolAnalyzerTest {

    @TempDir
    lateinit var tempDir: Path

    fun runAnalysis(source: String, moduleName: String = "test-module"): List<InternalNameEntry> {
        val srcDir = tempDir.resolve("src").createDirectories()
        val sourceFile = srcDir.resolve("Test.kt")
        sourceFile.writeText(source)

        val outputDir = tempDir.resolve("output").createDirectories()

        // Get current classpath to use as dependencies for resolution
        val classpath = System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .filter { it.endsWith(".jar") || File(it).isDirectory }

        val jdkHome = System.getProperty("java.home")

        val arguments = AnalysisArguments(
            projectName = "test-project",
            projectCoordinates = ProjectCoordinates("test", "test", "1.0"),
            outputDir = outputDir.toString(),
            sourceSets = listOf(
                AnalysisArguments.SourceSet(
                    name = moduleName,
                    platform = "jvm",
                    sources = listOf(srcDir.toString()),
                    classpath = classpath,
                    jdkHome = jdkHome
                )
            )
        )

        AnalysisSession(arguments).use { session ->
            session.analyze { sourceSet ->
                SymbolExportAnalyzer(
                    arguments.projectName,
                    arguments.projectCoordinates,
                    sourceSet.name,
                    outputDir
                )
            }
        }

        val outputFile = outputDir.resolve("$moduleName.json")
        if (!outputFile.exists()) return emptyList()

        return Json {
            isLenient = true
            ignoreUnknownKeys = true
        }.decodeFromString<List<InternalNameEntry>>(outputFile.readText())
    }
}
