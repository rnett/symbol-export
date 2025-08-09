package dev.rnett.symbolexport.symbol.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import dev.rnett.symbolexport.symbol.NameLike

/**
 * [KSName] does not implement equals, so equality checks will likely fail.
 *
 * Use [eq] or [matches] instead.
 *
 * See [google/ksp#2090](https://github.com/google/ksp/issues/2090).
 */
@Suppress("DEPRECATION")
@Deprecated("KSName does not implement equals, so equality checks will likely fail.")
public fun NameLike.asKsName(resolver: Resolver): KSName = resolver.getKsNameFromSymbol(this)

/**
 * [KSName] does not implement equals, so equality checks will likely fail.
 *
 * Use [eq] or [matches] instead.
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
public infix fun KSName?.eq(other: NameLike?): Boolean = this?.asString() == other?.asString()

/**
 * [KSName] does not implement equals, so this method compares it correctly to a symbol.
 *
 * See [google/ksp#2090](https://github.com/google/ksp/issues/2090).
 */
public infix fun NameLike?.eq(other: KSName?): Boolean = this?.asString() == other?.asString()

public fun KSName.matches(symbol: NameLike): Boolean = this eq symbol
public fun KSType.matches(symbol: NameLike): Boolean = declaration.matches(symbol)
public fun KSTypeReference.resolveMatches(symbol: NameLike): Boolean = resolve().matches(symbol)
public fun KSDeclaration.matches(symbol: NameLike): Boolean = this.qualifiedName eq symbol