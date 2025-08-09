package dev.rnett.symbolexport.symbol.compiler.annotation.fir

import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.compiler.toClassifier
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.name.ClassId


public fun AnnotationArgument.Companion.kClass(classId: ClassId): AnnotationArgument.KClass = AnnotationArgument.kClass(classId.toClassifier())
public fun AnnotationArgument.Companion.kClass(symbol: FirClassSymbol<*>): AnnotationArgument.KClass = AnnotationArgument.kClass(symbol.classId)

public fun AnnotationArgument.Companion.enum(classId: ClassId, name: String): AnnotationArgument.EnumEntry =
    AnnotationArgument.enum(classId.toClassifier(), name)

public fun AnnotationArgument.Companion.enum(symbol: FirClassSymbol<*>, name: String): AnnotationArgument.EnumEntry =
    AnnotationArgument.enum(symbol.classId, name)
