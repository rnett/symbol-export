package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalNameEntry
import kotlinx.serialization.json.Json
import java.io.File

object SymbolFileReader {
    private val json = Json {
        isLenient = true
    }

    fun readSymbolFiles(files: List<File>): List<InternalNameEntry> {
        return files.flatMap { readFile(it) }
    }

    private fun readFile(file: File): Set<InternalNameEntry> {
        return file.readLines().map { json.decodeFromString<InternalNameEntry>(it) }.toSet()
    }
}