package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.generator.Helpers.javadocString
import dev.rnett.symbolexport.generator.NameFromSourceSet.Companion.toNameFromSourceSet
import dev.rnett.symbolexport.generator.NameProject.Companion.toNameProject
import dev.rnett.symbolexport.internal.InternalNameEntry
import kotlinx.serialization.json.Json
import java.io.File


public class SymbolGenerator(
    private val outputDirectory: File,
    private val packageName: String,
    private val flattenProjects: Boolean
) {
    private val json = Json {
        isLenient = true
    }

    public fun writeSymbols(files: List<File>) {
        val entries = files.flatMap { readFile(it) }
        if (entries.isEmpty()) return

        val byProject =
            entries.groupBy({ it.toNameProject() }) { it.toNameFromSourceSet() }.mapValues { it.value.toSet() }

        if (flattenProjects) {
            writeFlatProjectFile(byProject)
        } else {
            writeFile(
                "Symbols.kt", """
                // symbols from multiple projects are present, and will be added to this object as extensions
                object Symbols
            """.trimIndent()
            )

            writeNestedProjectFile(byProject)

        }
    }

    private fun NameProject.symbolsCommentString(): String =
        "Symbols from project `$projectName` with coordinates `${projectCoordinates.group}:${projectCoordinates.artifact}:${projectCoordinates.version}`"


    private fun writeNestedProjectFile(projects: Map<NameProject, Set<NameFromSourceSet>>) {
        writeSymbolsFile(buildString {
            projects.forEach { (project, names) ->
                val objectString =
                    generateSymbolsObject(project.projectName, names, javadocPrefix = project.symbolsCommentString())
                appendLine("// ${project.symbolsCommentString()}")
                appendLine()

                append(objectString)
            }
        })
    }

    private fun writeFlatProjectFile(projects: Map<NameProject, Set<NameFromSourceSet>>) {
        writeSymbolsFile(buildString {
            projects.forEach { (project, names) ->
                appendLine()
                appendLine("// ${project.symbolsCommentString()}")
                appendLine()

                appendLine(ProjectObjectGenerator(null, names).generate(null))

                appendLine("// End `${project.projectName}`")
            }
        })
    }

    private fun writeSymbolsFile(content: String) {
        writeFile("Symbols.kt", buildString {
            appendLine(javadocString("Symbols from all projects"))
            appendLine("object Symbols {")

            appendLine(content.trimEnd().replaceIndent("    "))

            appendLine()
            appendLine("}")
        })
    }

    private fun generateSymbolsObject(
        objectName: String?,
        names: Set<NameFromSourceSet>,
        javadocPrefix: String? = null
    ): String? =
        ProjectObjectGenerator(objectName, names).generate(javadocPrefix)

    private fun readFile(file: File): Set<InternalNameEntry> {
        return file.readLines().map { json.decodeFromString<InternalNameEntry>(it) }.toSet()
    }

    private fun writeFile(name: String, content: String?) {
        if (content == null) return

        createFile(name).writeText(
            buildString {
                append(premable(packageName))
                appendLine()
                append(content)
            }
        )

    }

    private fun premable(packageName: String) = """
        @file:Suppress("RemoveRedundantBackticks", "RedundantVisibilityModifier", "ClassName")
        
        package $packageName
        
        import dev.rnett.symbolexport.symbol.Symbol.Classifier
        import dev.rnett.symbolexport.symbol.Symbol.ClassifierMember
        import dev.rnett.symbolexport.symbol.Symbol.TopLevelMember
        import dev.rnett.symbolexport.symbol.NameSegments
        import dev.rnett.symbolexport.symbol.Symbol
        
    """.trimIndent()


    private fun createFile(name: String): File {
        return outputDirectory.resolve(packageName.replace(".", "/")).resolve(name).also {
            it.parentFile.mkdirs()
            it.createNewFile()
        }
    }

}

