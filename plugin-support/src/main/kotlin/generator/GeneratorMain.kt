package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalNameSerializer
import dev.rnett.symbolexport.postprocessor.ExportedSymbols
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText

/**
 * Entry point for the symbol export code generator.
 *
 * It is responsible for reading serialized symbol files and invoking [Generator] to produce Kotlin code.
 */
public object GeneratorMain {
    @JvmStatic
    public fun main(args: Array<String>) {
        val outputDir = Path(args.getOrElse(0) { throw IllegalArgumentException("Output directory argument is required") })
        val basePackage = args.getOrElse(1) { throw IllegalArgumentException("Base package argument is required") }
        val inputs = args.drop(1)
            .zipWithNext { a, b -> a to Path(b) }
            .toMap()
        val symbols = inputs
            .mapValues { readDependencySymbols(it.value) }
    }

    private fun readDependencySymbols(file: Path): ExportedSymbols.V1 {
        return InternalNameSerializer.deserialize(file.readText())
    }
}