package dev.rnett.symbolexport.generator

internal inline fun StringBuilder.indent(indent: String = "    ", block: StringBuilder.() -> Unit) {
    append(buildString(block).replaceIndent(indent))
}