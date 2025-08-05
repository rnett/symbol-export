package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalNameEntry
import dev.rnett.symbolexport.internal.InternalNameSerializer
import java.io.File

object SymbolFileReader {
    fun readSymbolFiles(files: List<File>): List<InternalNameEntry> {
        return files.flatMap { readFile(it) }
    }

    private fun readFile(file: File): Set<InternalNameEntry> {
        return file.readLines().map { InternalNameSerializer.deserializeEntry(it) }.toSet()
    }
}