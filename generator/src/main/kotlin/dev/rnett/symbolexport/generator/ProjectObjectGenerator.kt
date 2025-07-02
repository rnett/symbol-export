package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName

internal class ProjectObjectGenerator(
    val objectName: String?,
    names: Set<NameFromSourceSet>
) {

    val symbols = names.groupBy({ NameSourceSet(it.sourceSet) }) { it.name }.mapValues { it.value.toSet() }

    fun generate(javadocPrefix: String?): String? {
        if (symbols.isEmpty()) return null

        return if (symbols.size == 1) {
            generateSingle(symbols.keys.single(), symbols.values.single(), javadocPrefix = javadocPrefix)
        } else {
            val commonMain = symbols[NameSourceSet.COMMON_MAIN]
            val updated = symbols.minus(NameSourceSet.COMMON_MAIN).mapValues {
                if (commonMain != null)
                    it.value.minus(commonMain)
                else
                    it.value
            }
            generateMultiplePlatforms(commonMain, updated.filter { it.value.isNotEmpty() }, javadocPrefix)
        }
    }

    private fun String.indentForObject() = indentForObject(objectName)

    private fun generateMultiplePlatforms(
        commonMain: Set<InternalName>? = null,
        otherPlatforms: Map<NameSourceSet, Set<InternalName>>,
        javadocPrefix: String?
    ): String {
        return buildString {
            val commentString = buildString {
                append("Generated from multiple source sets")
                if (commonMain != null) {
                    append(" - the top level is from `commonMain`")
                }
            }

            if (objectName != null) {
                appendLine(Helpers.javadocString(buildString {
                    if (javadocPrefix != null) {
                        appendLine(javadocPrefix)
                        appendLine()
                    }
                    appendLine(commentString)
                }))
                appendLine("public object `$objectName` {")
            } else {
                appendLine("// $commentString")
            }
            appendLine()

            if (commonMain != null) {
                appendLine(
                    generateSingleString(
                        null,
                        NameSourceSet.COMMON_MAIN,
                        commonMain,
                        javadocPrefix
                    ).indentForObject()
                )
            }

            otherPlatforms.forEach { (comp, symbols) ->
                appendLine(
                    generateSingleString(
                        comp.objectName(),
                        comp,
                        symbols,
                        null
                    ).indentForObject()
                )
                appendLine()
            }

            appendLine(
                generateAllSymbols(
                    commonMain.orEmpty(),
                    otherPlatforms.mapKeys { it.key.objectName() }
                ).replaceIndent("    ")
            )
            appendLine()
            if (objectName != null) {
                appendLine("}")
            }
        }
    }

    private fun generateSingle(
        sourceSet: NameSourceSet,
        symbols: Set<InternalName>,
        javadocPrefix: String?
    ): String {
        return buildString {
            appendLine()
            append(generateSingleString(objectName, sourceSet, symbols, javadocPrefix) {
                appendLine()
                appendLine(generateAllSymbols(symbols, emptyMap()).replaceIndent("    "))
                appendLine()
            })
        }
    }

    private fun generateAllSymbols(topLevelSymbols: Set<InternalName>, otherSymbols: Map<String, Set<InternalName>>) =
        generateAllSymbolsProperty(
            topLevelSymbols.map { it.fieldName() }.toSet() +
                    otherSymbols.flatMap {
                        val prefix = it.key.plus(".")
                        it.value.map { prefix + it.fieldName() }
                    }.toSet()
        )

    companion object {

        private fun generateSingleString(
            objectName: String?,
            sourceSet: NameSourceSet,
            symbols: Set<InternalName>,
            javadocPrefix: String?,
            additional: StringBuilder.() -> Unit = {}
        ) = buildString {
            appendLine()

            val commentString = "Generated from source set `${sourceSet.name}`"

            if (objectName != null) {

                val javadocStr = Helpers.javadocString(buildString {
                    if (javadocPrefix != null) {
                        appendLine(javadocPrefix)
                        appendLine()
                    }
                    append(commentString)
                })

                appendLine(javadocStr)
                appendLine("public object `$objectName` {")
            } else {
                appendLine("//  $commentString")
            }

            appendLine()

            append(generateProperties(symbols).indentForObject(objectName))

            appendLine()

            if (objectName == null) {
                appendLine()
                appendLine("// End` ${sourceSet.name}`")
            }

            appendLine()

            additional()

            if (objectName != null) {
                appendLine("}")
            }
        }

        private fun String.indentForObject(objectName: String?) =
            if (objectName == null) trimIndent() else replaceIndent("    ")

        fun generateAllSymbolsProperty(
            fields: Set<String>
        ) = buildString {
            appendLine("val ALL_SYMBOLS: Set<Symbol> = setOf(")
            fields.forEach {
                append("    ")
                append(it)
                appendLine(",")
            }

            appendLine(")")
        }


        private fun generateProperties(symbols: Set<InternalName>): String = buildString {
            symbols.forEach {
                appendLine()
                appendLine(Helpers.javadocString("Generated from `${it.allParts().joinToString(".")}`"))
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
}