package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION

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
                    objectName,
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
        return generateAllSymbolsProperty(
            topLevelSymbols.map { "`$realObjectName`.`${it.fieldName()}`" }.toSet() +
                    otherSymbols.flatMap {
                        val prefix = it.key.plus(".")
                        it.value.map { "$prefix`${it.fieldName()}`" }
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

            val commentString = "Generated from the source set `${sourceSet.name}`"

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


        fun generateProperties(symbols: Set<InternalName>): String = buildString {
            symbols.forEach {
                append(generateProperty(it))
            }
        }

        fun generateProperty(name: InternalName): String = buildString {
            appendLine()
            appendLine(Helpers.javadocString("Generated from `${name.allParts().joinToString(".")}`"))
            appendLine("public val `${name.fieldName()}`: ${name.type()} = ${name.constructor()}")
        }

        fun InternalName.constructor(): String = when (this) {
            is InternalName.Classifier -> "Classifier(packageName = ${nameSegmentsOf(packageName)}, classNames = ${
                nameSegmentsOf(
                    classNames
                )
            })"

            is InternalName.ClassifierMember -> "ClassifierMember(classifier = ${classifier.constructor()}, name = \"$name\")"

            is InternalName.TopLevelMember -> "TopLevelMember(packageName = ${nameSegmentsOf(packageName)}, name = \"$name\")"
            is InternalName.EnumEntry -> "EnumEntry(enumClass = ${owner.constructor()}, entryName = \"$name\", entryOrdinal = $ordinal)"
            is InternalName.Constructor -> "Constructor(classifier = ${classifier.constructor()}, name = \"$name\")"
            is InternalName.TypeParameter -> "TypeParameter(owner=${owner.constructor()}, index=$index, name=\"$name\")"
            is InternalName.IndexedParameter -> {
                val ctorName = when (type) {
                    VALUE -> "ValueParameter"
                    CONTEXT -> "ContextParameter"
                }

                val indexParam = when (type) {
                    VALUE -> "indexInValueParameters"
                    CONTEXT -> "indexInContextParameters"
                }

                "$ctorName(owner=${owner.constructor()}, index=$index, $indexParam=$indexInList, name=\"$name\")"
            }

            is InternalName.ReceiverParameter -> {
                val ctorName = when (type) {
                    EXTENSION -> "ExtensionReceiverParameter"
                    DISPATCH -> "DispatchReceiverParameter"
                }
                "$ctorName(owner=${owner.constructor()}, index=$index, name=\"$name\")"
            }
        }

        fun nameSegmentsOf(segments: List<String>): String =
            "NameSegments(${segments.joinToString(", ") { "\"$it\"" }})"

        fun InternalName.allParts(): List<String> = when (this) {
            is InternalName.Classifier -> packageName + classNames
            is InternalName.ClassifierMember -> classifier.allParts() + name
            is InternalName.TopLevelMember -> packageName + name
            is InternalName.EnumEntry -> owner.allParts() + name
            is InternalName.IndexedParameter -> owner.allParts() + name
            is InternalName.Constructor -> classifier.allParts() + name
            is InternalName.ReceiverParameter -> owner.allParts() + name
            is InternalName.TypeParameter -> owner.allParts() + name
        }

        fun InternalName.type(): String = when (this) {
            is InternalName.Classifier -> "Classifier"
            is InternalName.ClassifierMember -> "ClassifierMember"
            is InternalName.TopLevelMember -> "TopLevelMember"
            is InternalName.EnumEntry -> "EnumEntry"
            is InternalName.Constructor -> "Constructor"
            is InternalName.TypeParameter -> "TypeParameter"
            is InternalName.IndexedParameter -> when (type) {
                VALUE -> "ValueParameter"
                CONTEXT -> "ContextParameter"
            }

            is InternalName.ReceiverParameter -> when (type) {
                EXTENSION -> "ExtensionReceiverParameter"
                DISPATCH -> "DispatchReceiverParameter"
            }
        }

        fun InternalName.fieldName() = allParts().joinToString("_") { it.replace("<", "").replace(">", "") }
    }
}