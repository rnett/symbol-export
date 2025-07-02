package dev.rnett.symbolexport.symbol.compiler

import dev.rnett.symbolexport.symbol.NameLike
import dev.rnett.symbolexport.symbol.Symbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

public fun NameLike.asFqName(): FqName = FqName.fromSegments(this.segments)

public fun Symbol.Classifier.asClassId(): ClassId = ClassId(packageName.asFqName(), classNames.asFqName(), false)

public fun Symbol.Member.name(): Name = Name.identifier(name)