package dev.rnett.symbolexport

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
public value class SymbolTarget(public val name: String) {

    internal val isAll: Boolean
        get() = name == all.name
    internal val isCommon: Boolean
        get() = name == common.name

    internal companion object {
        val all = SymbolTarget($$$"$$all$$")

        val common = SymbolTarget($$$"$$common$$")
    }
}