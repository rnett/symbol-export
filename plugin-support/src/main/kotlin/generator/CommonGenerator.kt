package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.CodeBlock
import dev.rnett.symbolexport.internal.InternalSymbol

/**
 * Shared utilities for generating common Kotlin code structures like names and signatures.
 */
internal object CommonGenerator {
    fun qName(symbol: InternalSymbol): CodeBlock {
        return when (symbol) {
            is InternalSymbol.Classifier -> {
                val pkg = CodeBlock.of("%T(listOf(%L))", Names.PackageName, symbol.packageNames.joinToString(", ") { "\"$it\"" })
                CodeBlock.of("%T(%L, listOf(%L))", Names.ClassName, pkg, symbol.classNames.joinToString(", ") { "\"$it\"" })
            }

            is InternalSymbol.Member -> {
                if (symbol.classNames == null) {
                    val pkg = CodeBlock.of("%T(listOf(%L))", Names.PackageName, symbol.packageNames.joinToString(", ") { "\"$it\"" })
                    CodeBlock.of("%T(%L, %S)", Names.TopLevelCallableName, pkg, symbol.name)
                } else {
                    val pkg = CodeBlock.of("%T(listOf(%L))", Names.PackageName, symbol.packageNames.joinToString(", ") { "\"$it\"" })
                    val cls = CodeBlock.of("%T(%L, listOf(%L))", Names.ClassName, pkg, symbol.classNames.orEmpty().joinToString(", ") { "\"$it\"" })
                    CodeBlock.of("%T(%L, %S)", Names.MemberName, cls, symbol.name)
                }
            }

            is InternalSymbol.EnumEntry -> {
                val pkg = CodeBlock.of("%T(listOf(%L))", Names.PackageName, symbol.owner.packageNames.joinToString(", ") { "\"$it\"" })
                val cls = CodeBlock.of("%T(%L, listOf(%L))", Names.ClassName, pkg, symbol.owner.classNames.joinToString(", ") { "\"$it\"" })
                CodeBlock.of("%T(%L, %S)", Names.MemberName, cls, symbol.name)
            }
        }
    }

    fun sig(symbol: InternalSymbol.Function): CodeBlock {
        val params = symbol.parameterSignatures.map { param ->
            CodeBlock.of("%T(%S, %L, %L)", Names.ParamSignature, param.name, param.hasDefaultValue, typeSig(param.type))
        }
        return CodeBlock.of("%T(listOf(%L))", Names.FunctionSignature, params.joinToString(", "))
    }

    private fun typeSig(type: InternalSymbol.ParamType): CodeBlock {
        return when (type) {
            is InternalSymbol.ParamType.ClassBased -> {
                val args = type.arguments.map { arg ->
                    when (arg) {
                        is InternalSymbol.ParamTypeArg.Wildcard -> CodeBlock.of("%T", Names.Wildcard)
                        is InternalSymbol.ParamTypeArg.TypeProjection -> {
                            val variance = when (arg.variance) {
                                InternalSymbol.ParamTypeArg.Variance.INVARIANT -> CodeBlock.of("%T.Variance.INVARIANT", Names.TypeArgumentSignature)
                                InternalSymbol.ParamTypeArg.Variance.IN -> CodeBlock.of("%T.Variance.IN", Names.TypeArgumentSignature)
                                InternalSymbol.ParamTypeArg.Variance.OUT -> CodeBlock.of("%T.Variance.OUT", Names.TypeArgumentSignature)
                            }
                            CodeBlock.of("%T(%L, %L)", Names.Projection, typeSig(arg.type), variance)
                        }
                    }
                }
                CodeBlock.of("%T(%S, %L, listOf(%L))", Names.ClassBased, type.classifierFqn, type.isNullable, args.joinToString(", "))
            }

            is InternalSymbol.ParamType.TypeParam -> {
                CodeBlock.of("%T(%S, %L)", Names.TypeParam, type.name, type.isNullable)
            }

            is InternalSymbol.ParamType.Dynamic -> {
                CodeBlock.of("%T", Names.Dynamic)
            }
        }
    }
}
