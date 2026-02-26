package dev.rnett.symbolexport.ir

import dev.rnett.kcp.development.utils.ir.ExperimentalIrHelpers
import dev.rnett.kcp.development.utils.ir.WithIrContext
import dev.rnett.symbolexport.internal.InternalSymbol
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrScriptSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.IrErrorType
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.util.callableId
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.isNullable
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.types.Variance

@OptIn(ExperimentalIrHelpers::class)
class SymbolFactory(override val context: IrPluginContext) : WithIrContext {
    fun classSymbol(declaration: IrClass): InternalSymbol.Classifier {
        return InternalSymbol.Classifier(
            declaration.classId!!.packageFqName.pathSegments().map { it.asString() },
            declaration.classId!!.relativeClassName.pathSegments().map { it.asString() },
        )
    }

    fun functionSymbol(declaration: IrFunction): InternalSymbol.Function {
        return InternalSymbol.Function(
            declaration.callableId.packageName.pathSegments().map { it.asString() },
            declaration.callableId.className?.pathSegments()?.map { it.asString() },
            declaration.name.asString(),
            declaration.parameters.map { getParameterSignature(it) },
            declaration.findExportedName()
        )
    }

    fun propertySymbol(declaration: IrProperty): InternalSymbol.Property {
        return InternalSymbol.Property(
            declaration.callableId.packageName.pathSegments().map { it.asString() },
            declaration.callableId.className?.pathSegments()?.map { it.asString() },
            declaration.name.asString(),
            declaration.findExportedName()
        )
    }

    fun enumEntrySymbol(declaration: IrEnumEntry): InternalSymbol.EnumEntry {
        return InternalSymbol.EnumEntry(
            classSymbol(declaration.parentAsClass),
            declaration.name.identifier,
            declaration.parentAsClass.declarations.filterIsInstance<IrEnumEntry>().indexOf(declaration)
        )
    }

    fun getParameterSignature(param: IrValueParameter): InternalSymbol.ParameterSignature {
        return InternalSymbol.ParameterSignature(
            param.name.asString(),
            param.hasDefaultValue(),
            param.type.asParamType()
        )
    }

    private fun IrType.asParamType(): InternalSymbol.ParamType {
        return when (this) {
            is IrDynamicType -> InternalSymbol.ParamType.Dynamic
            is IrErrorType -> error("Error types not supported")
            is IrSimpleType -> when (val cls = classifier) {
                is IrClassSymbol -> InternalSymbol.ParamType.ClassBased(
                    cls.owner.kotlinFqName.asString(),
                    this.isNullable(),
                    arguments.map { it.asParamTypeArg() }
                )

                is IrScriptSymbol -> error("Scripts not supported")
                is IrTypeParameterSymbol -> InternalSymbol.ParamType.TypeParam(cls.owner.name.asString(), this.isNullable())
            }
        }
    }

    private fun IrTypeArgument.asParamTypeArg(): InternalSymbol.ParamTypeArg {
        return when (this) {
            is IrStarProjection -> InternalSymbol.ParamTypeArg.Wildcard
            is IrTypeProjection -> InternalSymbol.ParamTypeArg.TypeProjection(
                variance.toModel(),
                type.asParamType()
            )
        }
    }

    private fun Variance.toModel(): InternalSymbol.ParamTypeArg.Variance {
        return when (this) {
            Variance.INVARIANT -> InternalSymbol.ParamTypeArg.Variance.INVARIANT
            Variance.IN_VARIANCE -> InternalSymbol.ParamTypeArg.Variance.IN
            Variance.OUT_VARIANCE -> InternalSymbol.ParamTypeArg.Variance.OUT
        }
    }
}
