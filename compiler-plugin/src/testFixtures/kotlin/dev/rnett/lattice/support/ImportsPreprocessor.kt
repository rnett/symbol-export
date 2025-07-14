package dev.rnett.lattice.support

import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.services.ReversibleSourceFilePreprocessor
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.isKtFile

class ImportsPreprocessor(imports: Set<String>, testServices: TestServices) :
    ReversibleSourceFilePreprocessor(testServices) {

    val importsString = "\n" + imports.joinToString("\n") { "import $it" } + "\n"

    override fun revert(file: TestFile, actualContent: String): String {
        if (!file.isKtFile) return actualContent
        return actualContent.replaceFirst(importsString, "")
    }

    override fun process(file: TestFile, content: String): String {
        if (!file.isKtFile) return content

        if (content.lines().any { it.startsWith("package") }) {
            return buildString {
                content.lines().forEach {
                    appendLine(it)
                    if (it.trim().startsWith("package")) {
                        append(importsString)
                    }
                }
            }
        } else {
            return importsString + content
        }
    }
}