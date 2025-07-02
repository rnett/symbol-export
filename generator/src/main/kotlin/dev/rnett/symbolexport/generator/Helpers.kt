package dev.rnett.symbolexport.generator

internal object Helpers {
    fun javadocString(content: String) = buildString {
        appendLine("/**")
        content.lineSequence().forEach {
            appendLine(" * $it")
        }
        append(" */")
    }
}

internal inline fun StringBuilder.indent(indent: String = "    ", block: StringBuilder.() -> Unit) {
    append(buildString(block).replaceIndent(indent))
}