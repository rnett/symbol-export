package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName

/**
 * Handles code formatting, indentation, and string generation utilities
 */
internal object CodeFormatter {
    const val INDENT = "    "

    fun indentForObject(content: String, objectName: String?): String =
        if (objectName == null) content.trimIndent() else content.replaceIndent(INDENT)

    fun generateJavadocComment(javadocPrefix: String?, additionalComment: String): String {
        return javadocString(buildString {
            if (javadocPrefix != null) {
                appendLine(javadocPrefix)
                appendLine()
            }
            append(additionalComment)
        })
    }

    fun generateObjectHeader(objectName: String?, javadocPrefix: String?, comment: String, useDoubleSpace: Boolean = true): String {
        return buildString {
            if (objectName != null) {
                appendLine(generateJavadocComment(javadocPrefix, comment))
                appendLine("public object `$objectName` {")
            } else {
                val spacing = if (useDoubleSpace) "//  " else "// "
                appendLine("$spacing$comment")
            }
        }
    }

    fun generateObjectFooter(objectName: String?): String {
        return if (objectName != null) "}" else ""
    }

    fun generateSymbol(name: InternalName, referencable: Set<InternalName>): String = buildString {
        val cls = InternalNameGenerationHandler.generateClass(name, referencable)
        if (cls != null) {
            appendLine()
            appendLine(javadocString("Generated from `${InternalNameGenerationHandler.getAllParts(name).joinToString(".")}`"))
            appendLine("public ${cls.removePrefix("public")}")
        }
        appendLine()
        appendLine(javadocString("Generated from `${InternalNameGenerationHandler.getAllParts(name).joinToString(".")}`"))
        appendLine("public val `${InternalNameGenerationHandler.getFieldName(name)}`: ${InternalNameGenerationHandler.getType(name)} = ${InternalNameGenerationHandler.generateConstructor(name, referencable)}")
    }

    fun generateSymbol(symbols: Set<InternalName>): String = buildString {
        symbols.forEach {
            append(generateSymbol(it, symbols))
        }
    }

    fun generateAllSymbolsProperty(fields: Set<String>): String = buildString {
        appendLine("val ALL_SYMBOLS: Set<Symbol> = setOf(")
        fields.forEach {
            append(INDENT)
            append(it)
            appendLine(",")
        }
        appendLine(")")
    }

    fun generateSourceSetComment(sourceSet: NameSourceSet): String =
        "Generated from the source set `${sourceSet.name}`"

    fun generateMultiplePlatformsComment(hasCommonMain: Boolean): String = buildString {
        append("Generated from multiple source sets")
        if (hasCommonMain) {
            append(" - the top level is from `commonMain`")
        }
    }

    fun javadocString(content: String) = buildString {
        appendLine("/**")
        content.lineSequence().forEach {
            appendLine(" * $it")
        }
        append(" */")
    }
}
