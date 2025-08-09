package test.cases


fun interface SourceGenerator {
    fun generateSource(caseName: String): String
}

data class Sources(val setups: List<SourceGenerator>, val assertions: List<SourceGenerator>) {
    companion object {
        fun default() = Sources(listOf(), listOf())
    }

    fun source(caseName: String) = buildString {
        appendLine()
        setups.forEach {
            appendLine(it.generateSource(caseName)).trim()
            appendLine()
        }
        appendLine()
        appendLine("fun box(): String {")
        append(buildString {
            assertions.forEach {
                appendLine(it.generateSource(caseName))
                appendLine()
            }
            appendLine()
            appendLine("return \"OK\"")
        }.replaceIndent("    "))
        appendLine()

        appendLine("}")
    }
}