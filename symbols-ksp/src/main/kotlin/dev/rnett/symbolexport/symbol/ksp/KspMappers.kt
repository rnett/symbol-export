package dev.rnett.symbolexport.symbol.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSName
import dev.rnett.symbolexport.symbol.NameLike

/**
 * [KSName] does not implement equals, so equality checks will likely fail.
 *
 * See [google/ksp#2090](https://github.com/google/ksp/issues/2090).
 */
@Suppress("DEPRECATION")
@Deprecated("KSName does not implement equals, so equality checks will likely fail.")
public fun NameLike.asKsName(resolver: Resolver): KSName = resolver.getKsNameFromSymbol(this)

/**
 * [KSName] does not implement equals, so equality checks will likely fail.
 *
 * See [google/ksp#2090](https://github.com/google/ksp/issues/2090).
 */
@Deprecated("KSName does not implement equals, so equality checks will likely fail.")
public fun Resolver.getKsNameFromSymbol(symbol: NameLike): KSName = getKSNameFromString(symbol.asString())

/**
 * [KSName] does not implement equals, so this method compares it correctly to a symbol.
 *
 * See [google/ksp#2090](https://github.com/google/ksp/issues/2090).
 */
public infix fun KSName.eq(other: NameLike): Boolean = this.asString() == other.asString()

/**
 * [KSName] does not implement equals, so this method compares it correctly to a symbol.
 *
 * See [google/ksp#2090](https://github.com/google/ksp/issues/2090).
 */
public infix fun NameLike.eq(other: KSName): Boolean = this.asString() == other.asString()