package com.rnett.symbolexport.symbol.compiler

import com.rnett.symbolexport.symbol.NameLike
import com.rnett.symbolexport.symbol.Symbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

public fun NameLike.asFqName(): FqName = FqName.fromSegments(this.segments)

public fun Symbol.Classifier.asClassId() = ClassId(packageName.asFqName(), classNames.asFqName(), false)

public fun Symbol.Member.name() = Name.identifier(name)