package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.generator.Helpers.javadocString
import dev.rnett.symbolexport.generator.NameFromSourceSet.Companion.toNameFromSourceSet
import dev.rnett.symbolexport.generator.NameProject.Companion.toNameProject
import dev.rnett.symbolexport.internal.InternalNameEntry


internal class SymbolGenerator(
    private val packageName: String,
    private val flattenProjects: Boolean,
    private val objectGenerator: ProjectObjectGenerator
) {

    // this should be the only entrypoint used. Others are exposed for testing
    fun generateSymbolsFile(entries: List<InternalNameEntry>): String? {
        val byProject =
            entries.groupBy({ it.toNameProject() }) { it.toNameFromSourceSet() }.mapValues { it.value.toSet() }

        if (flattenProjects) {
            return generateFlatProjectFile(byProject)
        } else {
            return generateNestedProjectFile(byProject)
        }
    }

    private fun NameProject.symbolsCommentString(): String =
        "Symbols from project `$projectName` with coordinates `${projectCoordinates.group}:${projectCoordinates.artifact}:${projectCoordinates.version}`"


    fun generateNestedProjectFile(projects: Map<NameProject, Set<NameFromSourceSet>>): String? {
        return generateSymbolsFile(buildString {
            projects.forEach { (project, names) ->
                val objectString =
                    generateSymbolsObject(project.projectName, names, javadocPrefix = project.symbolsCommentString())
                appendLine("// ${project.symbolsCommentString()}")
                appendLine()

                append(objectString)
            }
        })
    }

    fun generateFlatProjectFile(projects: Map<NameProject, Set<NameFromSourceSet>>): String? {
        return generateSymbolsFile(buildString {
            projects.forEach { (project, names) ->
                appendLine()
                appendLine("// ${project.symbolsCommentString()}")
                appendLine()

                appendLine(objectGenerator.generate(null, names, null))

                appendLine("// End `${project.projectName}`")
            }
        })
    }

    fun generateSymbolsFile(content: String): String? {
        return generateFile(buildString {
            appendLine(javadocString("Symbols from all projects"))
            appendLine("internal object Symbols {")

            appendLine(content.trimEnd().replaceIndent("    "))

            appendLine()
            appendLine("}")
        })
    }

    fun generateSymbolsObject(
        objectName: String?,
        names: Set<NameFromSourceSet>,
        javadocPrefix: String? = null
    ): String? =
        objectGenerator.generate(objectName, names, javadocPrefix)

    private fun generateFile(content: String?): String? {
        if (content == null) return null

        return buildString {
            append(premable(packageName))
            appendLine()
            append(content)
        }

    }

    private fun premable(packageName: String) = """
        @file:Suppress("RemoveRedundantBackticks", "RedundantVisibilityModifier", "ClassName")
        
        package $packageName
        
        import dev.rnett.symbolexport.symbol.*
        import dev.rnett.symbolexport.symbol.Symbol.*
        
    """.trimIndent()


}

