package com.rnett.symbolexport.import

import com.rnett.symbolexport.Shared
import com.rnett.symbolexport.internal.InternalName
import kotlinx.serialization.json.Json
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

internal object SymbolGenerator {
    val json = Json {
        isLenient = true
    }

    val commonMainSourceSet = SourceSet("commonMain")

    fun generateSymbols(files: List<File>, outputFile: File, get: String) {
        if (outputFile.exists()) {
            outputFile.deleteRecursively()
        }

        outputFile.parentFile.mkdirs()

        val symbols = files.associate { sourceSetForFile(it) to readFile(it) }
        if (symbols.isEmpty()) return

        if (symbols.size == 1) {
            generateSingle(outputFile, symbols.keys.single(), symbols.values.single())
        } else {
            val commonMain = symbols[commonMainSourceSet]
            val updated = symbols.minus(commonMainSourceSet).mapValues {
                if (commonMain != null)
                    it.value.minus(commonMain)
                else
                    it.value
            }
            generateMultiple(outputFile, commonMain, updated.filter { it.value.isNotEmpty() })
        }
    }

    data class SourceSet(val name: String) {
        fun objectName() = name.capitalized()
    }

    private fun sourceSetForFile(file: File): SourceSet {
        val name = file.name.removeSuffix(Shared.SYMBOLS_FILE_EXTENSION).removePrefix(Shared.SYMBOLS_FILE_PREFIX)
        return SourceSet(name)
    }

    private fun readFile(file: File): Set<InternalName> =
        file.readLines().map { json.decodeFromString<InternalName>(it) }.toSet()

    private fun generateMultiple(
        outputFile: File,
        commonMain: Set<InternalName>? = null,
        otherPlatforms: Map<SourceSet, Set<InternalName>>
    ) {
        outputFile.writeText(
            buildString {
                appendLine(imports)
                appendLine()

                append("// Generated from multiple source sets")
                if (commonMain != null) {
                    append(" - the top level is from commonMain")
                }
                appendLine()

                appendLine("public object Symbols {")
                appendLine()

                if (commonMain != null) {
                    appendLine("    // Common main symbols")
                    appendLine()
                    append(generateProperties(commonMain).replaceIndent("    "))
                    appendLine()
                    appendLine()
                }

                otherPlatforms.forEach { (comp, symbols) ->
                    appendLine(generateSingleString(comp.objectName(), comp, symbols).replaceIndent("    "))
                    appendLine()
                }

                appendLine("}")
            }
        )
    }

    private fun generateSingle(outputFile: File, sourceSet: SourceSet, symbols: Set<InternalName>) {
        outputFile.writeText(buildString {
            appendLine(imports)
            appendLine()
            append(generateSingleString("Symbols", sourceSet, symbols))
        })
    }

    val imports = """
        import com.rnett.symbolexport.symbol.Symbol.Classifier
        import com.rnett.symbolexport.symbol.Symbol.ClassifierMember
        import com.rnett.symbolexport.symbol.Symbol.TopLevelMember
        import com.rnett.symbolexport.symbol.NameSegments
    """.trimIndent()

    private fun generateSingleString(name: String, sourceSet: SourceSet, symbols: Set<InternalName>) = buildString {
        appendLine("// Generated from ${sourceSet.name}")
        appendLine("public object $name {")

        append(generateProperties(symbols).replaceIndent("    "))

        appendLine()
        appendLine("}")
    }

    private fun generateProperties(symbols: Set<InternalName>): String = buildString {
        symbols.forEach {
            appendLine()
            appendLine("// Generated from ${it.allParts().joinToString(".")}")
            appendLine("public val `${it.fieldName()}`: ${it.type()} = ${it.constructor()}")
        }
    }

    private fun InternalName.constructor(): String = when (this) {
        is InternalName.Classifier -> "Classifier(packageName = ${nameSegmentsOf(packageName)}, classNames = ${
            nameSegmentsOf(
                classNames
            )
        })"

        is InternalName.ClassifierMember -> "ClassifierMember(classifier = ${classifier.constructor()}, name = \"$name\")"

        is InternalName.TopLevelMember -> "TopLevelMember(packageName = ${nameSegmentsOf(packageName)}, name = \"$name\")"
    }

    private fun nameSegmentsOf(segments: List<String>): String =
        "NameSegments(${segments.joinToString(", ") { "\"$it\"" }})"

    private fun InternalName.allParts(): List<String> = when (this) {
        is InternalName.Classifier -> packageName + classNames
        is InternalName.ClassifierMember -> classifier.allParts() + name
        is InternalName.TopLevelMember -> packageName + name
    }

    private fun InternalName.type(): String = when (this) {
        is InternalName.Classifier -> "Classifier"
        is InternalName.ClassifierMember -> "ClassifierMember"
        is InternalName.TopLevelMember -> "TopLevelMember"
    }

    private fun InternalName.fieldName() = allParts().joinToString("_")
}