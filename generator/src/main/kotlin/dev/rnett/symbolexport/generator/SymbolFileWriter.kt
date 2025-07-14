package dev.rnett.symbolexport.generator

import java.io.File

object SymbolFileWriter {
    fun generateAndWriteSymbolFiles(sourceFiles: List<File>, outputDirectory: File, packageName: String, flattenProjects: Boolean) {
        val symbols = SymbolFileReader.readSymbolFiles(sourceFiles)

        val fileText = SymbolGenerator(
            packageName,
            flattenProjects,
            DefaultProjectObjectGenerator.Generator
        ).generateSymbolsFile(symbols)

        if (fileText == null)
            return


        val file = outputDirectory.resolve(packageName.replace(".", "/")).resolve("Symbols.kt").also {
            it.parentFile.mkdirs()
            if (it.exists())
                it.delete()
            it.createNewFile()
        }

        file.writeText(fileText)

    }
}