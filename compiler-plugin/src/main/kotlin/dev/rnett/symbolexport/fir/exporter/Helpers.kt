package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import dev.rnett.symbolexport.internal.ParameterType
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol

object Helpers {

    /**
     * The index offset for the type, according to the Kotlin compiler's parameter ordering:
     * `[dispatch receiver, context parameters, extension receiver, value parameters]`.
     */
    fun getIndexStartOffset(symbol: FirCallableSymbol<*>, type: ParameterType): Int {
        return when (type) {
            DISPATCH -> 0
            CONTEXT -> if (symbol.dispatchReceiverType != null) 1 else 0
            EXTENSION -> getIndexStartOffset(symbol, CONTEXT) + symbol.contextParameterSymbols.size
            VALUE -> getIndexStartOffset(symbol, EXTENSION) + if (symbol.receiverParameterSymbol != null) 1 else 0
        }
    }
}