package dev.rnett.symbolexport.postprocessor

import dev.rnett.symbolexport.SymbolTarget
import dev.rnett.symbolexport.internal.InternalDeclaration
import dev.rnett.symbolexport.internal.InternalSymbol
import kotlinx.serialization.Serializable

@Serializable
public data class SymbolTree(val targets: Map<SymbolTarget, TargetSymbolTree>) {
    internal companion object {
        fun fromTargets(targetSymbols: TargetSymbols): SymbolTree {
            val targetMaps = mutableMapOf<SymbolTarget, TargetSymbolTree>()
            targetSymbols.forTargets { target, symbols ->
                targetMaps[target] = buildTree(symbols) {
                    targetSymbols.targetsFor(it)
                }
            }

            return SymbolTree(targetMaps)
        }

        private inline fun buildTree(symbols: Set<LoadedSymbol>, symbolTargets: (InternalSymbol) -> Set<SymbolTarget>): TargetSymbolTree {
            val root = TargetSymbolTreeBuilder("")

            symbols.forEach { symbol ->
                val node = root.descendent(symbol.symbol.qualifiedNameSegments)
                node.ownSymbol = symbol
                node.ownSymbolTargets.addAll(symbolTargets(symbol.symbol))
            }

            return root.build()
        }
    }
}

@Serializable
public data class TargetSymbolTree(val name: String, val ownSymbol: TargetSymbol?, val children: Map<String, TargetSymbolTree>) {
    internal fun child(name: String): TargetSymbolTree? = children[name]
    internal fun descendent(nameSegments: List<String>): TargetSymbolTree? {
        if (nameSegments.isEmpty()) return this
        if (nameSegments.size == 1) return child(nameSegments[0])
        return children[nameSegments.first()]?.descendent(nameSegments.subList(1, nameSegments.size))
    }
}

/**
 * [targets] won't ever contain [SymbolTarget.all].
 */
@Serializable
public data class TargetSymbol(val symbol: InternalSymbol, val declaration: InternalDeclaration?, val targets: Set<SymbolTarget>)

internal data class TargetSymbolTreeBuilder(val name: String) {
    var ownSymbol: LoadedSymbol? = null
    val ownSymbolTargets: MutableSet<SymbolTarget> = mutableSetOf()
    private val children: MutableMap<String, TargetSymbolTreeBuilder> = mutableMapOf()

    fun child(name: String): TargetSymbolTreeBuilder {
        return children.getOrPut(name) { TargetSymbolTreeBuilder(name) }
    }

    fun descendent(names: List<String>): TargetSymbolTreeBuilder {
        if (names.isEmpty()) return this
        if (names.size == 1) return child(names.first())
        return child(names.first()).descendent(names.subList(1, names.size))
    }

    fun build(): TargetSymbolTree {
        return TargetSymbolTree(name, ownSymbol?.let { TargetSymbol(it.symbol, it.declaration, ownSymbolTargets) }, children.mapValues { it.value.build() })
    }
}