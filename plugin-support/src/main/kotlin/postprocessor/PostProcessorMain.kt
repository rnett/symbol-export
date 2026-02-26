package dev.rnett.symbolexport.postprocessor

import dev.rnett.symbolexport.internal.InternalNameSerializer
import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.writeText

public object PostProcessorMain {
    @JvmStatic
    public fun main(args: Array<String>) {
        val rawSymbolsDir = Path(args.getOrElse(0) { error("No raw symbols directory provided") })
        val processedSymbolsFile = Path(args.getOrElse(1) { error("No processed symbols file provided") })
        val projectSymbolName = args.getOrElse(2) { error("No project symbol name provided") }

        if (!rawSymbolsDir.exists()) {
            error("Raw symbols directory does not exist: ${rawSymbolsDir.absolutePathString()}")
        }

        println("Processed symbols file: ${processedSymbolsFile.absolutePathString()}")

        val loaded = RawSymbolLoader.loadSymbols(rawSymbolsDir)

        val export: ExportedSymbols = ExportedSymbols.V1(
            projectSymbolName,
            SymbolTree.fromTargets(loaded)
        )
        processedSymbolsFile.deleteIfExists()
        processedSymbolsFile.createParentDirectories()
        processedSymbolsFile.writeText(InternalNameSerializer.serialize(export))
    }
}

@Serializable
public sealed interface ExportedSymbols {

    @Serializable
    public data class V1(
        val projectSymbolName: String,
        val symbols: SymbolTree
    ) : ExportedSymbols
}