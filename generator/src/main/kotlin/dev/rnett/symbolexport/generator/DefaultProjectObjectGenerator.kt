package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName

internal fun interface ProjectObjectGenerator {
    fun generate(objectName: String?, names: Set<NameFromSourceSet>, javadocPrefix: String?): String?
}

internal class DefaultProjectObjectGenerator(
    val objectName: String?,
    names: Set<NameFromSourceSet>
) {

    object Generator : ProjectObjectGenerator {
        override fun generate(objectName: String?, names: Set<NameFromSourceSet>, javadocPrefix: String?): String? {
            return DefaultProjectObjectGenerator(objectName, names).generate(javadocPrefix)
        }
    }

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

    fun generateMultiplePlatforms(
        commonMain: Set<InternalName>? = null,
        otherPlatforms: Map<NameSourceSet, Set<InternalName>>,
        javadocPrefix: String?
    ): String {
        return buildString {
            val commentString = CodeFormatter.generateMultiplePlatformsComment(commonMain != null)
            append(CodeFormatter.generateObjectHeader(objectName, javadocPrefix, commentString, useDoubleSpace = false))
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
                    objectName,
                    commonMain.orEmpty(),
                    otherPlatforms.mapKeys { it.key.objectName() }
                ).replaceIndent("    ")
            )
            appendLine()

            val footer = CodeFormatter.generateObjectFooter(objectName)
            if (footer.isNotEmpty()) {
                appendLine(footer)
            }
        }
    }

    fun generateSingle(
        sourceSet: NameSourceSet,
        symbols: Set<InternalName>,
        javadocPrefix: String?
    ): String {
        return buildString {
            appendLine()
            append(generateSingleString(objectName, sourceSet, symbols, javadocPrefix) {
                appendLine()
                appendLine(generateAllSymbols(objectName, symbols, emptyMap()).replaceIndent("    "))
                appendLine()
            })
        }
    }

    fun generateAllSymbols(
        objectName: String?,
        topLevelSymbols: Set<InternalName>,
        otherSymbols: Map<String, Set<InternalName>>
    ): String {
        val realObjectName = objectName ?: "Symbols"
        return CodeFormatter.generateAllSymbolsProperty(
            topLevelSymbols.map { "`$realObjectName`.`${InternalNameHandler.getFieldName(it)}`" }.toSet() +
                    otherSymbols.flatMap {
                        val prefix = it.key.plus(".")
                        it.value.map { "$prefix`${InternalNameHandler.getFieldName(it)}`" }
                    }.toSet()
        )
    }

    companion object {

        fun generateSingleString(
            objectName: String?,
            sourceSet: NameSourceSet,
            symbols: Set<InternalName>,
            javadocPrefix: String?,
            additional: StringBuilder.() -> Unit = {}
        ) = buildString {
            appendLine()

            val commentString = CodeFormatter.generateSourceSetComment(sourceSet)
            append(CodeFormatter.generateObjectHeader(objectName, javadocPrefix, commentString))
            appendLine()

            append(CodeFormatter.indentForObject(CodeFormatter.generateProperties(symbols), objectName))
            appendLine()

            if (objectName == null) {
                appendLine()
                appendLine("// End` ${sourceSet.name}`")
            }

            appendLine()
            additional()

            val footer = CodeFormatter.generateObjectFooter(objectName)
            if (footer.isNotEmpty()) {
                appendLine(footer)
            }
        }

        private fun String.indentForObject(objectName: String?) =
            CodeFormatter.indentForObject(this, objectName)

    }
}
