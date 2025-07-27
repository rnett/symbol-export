package dev.rnett.symbolexport.generator

internal inline fun StringBuilder.appendIndentedLine(block: StringBuilder.() -> Unit) {
    appendLine(buildString(block).replaceIndent(CodeFormatter.INDENT))
}