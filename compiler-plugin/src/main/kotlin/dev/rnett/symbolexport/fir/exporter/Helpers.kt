package dev.rnett.symbolexport.fir.exporter

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import dev.rnett.symbolexport.internal.ParameterType
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

object Helpers {

    fun createClassName(classSymbol: FirClassLikeSymbol<*>): InternalName.Classifier {
        return InternalName.Classifier(
            classSymbol.classId.packageFqName.pathSegments().map { it.asString() },
            classSymbol.classId.relativeClassName.pathSegments().map { it.asString() }
        )
    }

    fun createMemberName(symbol: FirCallableSymbol<*>, overrideName: Name? = null): InternalName.Member {
        val parent = symbol.getContainingClassSymbol()

        if (symbol is FirConstructorSymbol) {
            return InternalName.Constructor(createClassName(parent!!), SpecialNames.INIT.asString())
        }

        return if (parent == null) {
            InternalName.TopLevelMember(
                symbol.packageFqName().pathSegments().map { it.asString() },
                overrideName?.asString() ?: symbol.name.asString()
            )
        } else {
            InternalName.ClassifierMember(
                createClassName(parent),
                overrideName?.asString() ?: symbol.name.asString()
            )
        }
    }

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