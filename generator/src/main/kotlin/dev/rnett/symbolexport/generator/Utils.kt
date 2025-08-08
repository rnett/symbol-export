package dev.rnett.symbolexport.generator

internal inline fun StringBuilder.appendIndentedLine(indents: Int = 1, block: StringBuilder.() -> Unit) {
    appendLine(buildString(block).replaceIndent(CodeFormatter.INDENT.repeat(indents)))
}