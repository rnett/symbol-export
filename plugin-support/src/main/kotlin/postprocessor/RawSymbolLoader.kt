package dev.rnett.symbolexport.postprocessor

import dev.rnett.symbolexport.SymbolTarget
import dev.rnett.symbolexport.internal.InternalDeclaration
import dev.rnett.symbolexport.internal.InternalNameSerializer
import dev.rnett.symbolexport.internal.InternalSymbol
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText


internal data class LoadedSymbol(val symbol: InternalSymbol, val declaration: InternalDeclaration?)

internal data class TargetSymbols(private val map: Map<SymbolTarget, Set<LoadedSymbol>>) {
    private val all = map.values.flatMapTo(mutableSetOf()) { it }

    internal inline fun forTargets(block: (SymbolTarget, Set<LoadedSymbol>) -> Unit) {
        map.forEach { (target, loadedDeclarations) ->
            if (target.isAll) return@forEach
            block(target, loadedDeclarations)
        }
        if (SymbolTarget.common !in map)
            block(SymbolTarget.common, emptySet())
        block(SymbolTarget.all, all)
    }

    fun targetsFor(symbol: InternalSymbol): Set<SymbolTarget> = map.entries.asSequence().filter { it.value.any { it.symbol == symbol } }.map { it.key }.minus(SymbolTarget.all).toSet()
}

internal object RawSymbolLoader {
    fun loadSymbols(rawDir: Path): TargetSymbols {
        val sourceSets = rawDir.listDirectoryEntries().mapNotNull {
            if (!it.isDirectory()) return@mapNotNull null
            loadSourceSet(it)
        }


        val targetMaps = mutableMapOf<SymbolTarget, MutableMap<InternalSymbol, InternalDeclaration?>>()
        sourceSets.forEach { exports ->
            exports.targets.forEach { target ->
                val targetMap = targetMaps.getOrPut(SymbolTarget(target)) { mutableMapOf() }
                exports.symbols.forEach {
                    if (targetMap[it] != null) return@forEach
                    targetMap[it] = exports.findDeclaration(it)
                }
            }
        }

        val commonSymbols = buildSet {
            addAll(targetMaps.values.firstOrNull()?.keys.orEmpty())
            targetMaps.forEach { target ->
                retainAll(target.value.keys)
            }
        }

        targetMaps.values.forEach { map ->
            map.keys.removeAll(commonSymbols)
        }

        targetMaps[SymbolTarget.common] = commonSymbols.associateWith { s ->
            val allDeclarations = targetMaps.values.mapTo(mutableSetOf()) { it[s] }
            if (null in allDeclarations)
                null
            else
                allDeclarations.first()
        }.toMutableMap()

        return TargetSymbols(
            targetMaps.mapValues {
                it.value.entries.map { LoadedSymbol(it.key, it.value) }.toSet()
            }
        )

    }

    private fun loadSourceSet(sourceSetDir: Path): SourceSetSymbols {
        val declarationsPath = sourceSetDir.resolve("declarations.json")
        val targetsPath = sourceSetDir.resolve("targets.json")
        val symbolsPath = sourceSetDir.resolve("symbols.json")

        return SourceSetSymbols(
            deserializeIfExists<Set<String>>(targetsPath) ?: emptySet(),
            deserializeIfExists<Set<InternalSymbol>>(symbolsPath) ?: emptySet(),
            deserializeIfExists<Set<InternalDeclaration>>(declarationsPath) ?: emptySet(),
        )

    }

    private inline fun <reified R : Any> deserializeIfExists(path: Path): R? {
        return path.takeIf { it.exists() && it.isRegularFile() && it.isReadable() }?.let { InternalNameSerializer.deserialize<R>(it.readText()) }
    }

    private data class SourceSetSymbols(
        val targets: Set<String>,
        val symbols: Set<InternalSymbol>,
        val declarations: Set<InternalDeclaration>
    ) {
        fun findDeclaration(symbol: InternalSymbol) = declarations.firstOrNull { it.symbol == symbol }
    }
}