package dev.rnett.symbolexport.symbol.compiler

import dev.rnett.symbolexport.symbol.NameLike
import dev.rnett.symbolexport.symbol.NameSegments
import dev.rnett.symbolexport.symbol.Symbol
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

public fun NameLike.asFqName(): FqName = FqName.fromSegments(this.nameSegments)

public fun Symbol.ClassLike.asClassId(): ClassId = ClassId(packageName.asFqName(), classNames.asFqName(), false)

public fun Symbol.NamedSymbol.name(): Name = Name.identifierIfValid(name) ?: Name.special(name)

public fun Symbol.Member.asCallableId(): CallableId = when (this) {
    is Symbol.NamedClassifierMember -> CallableId(classifier.asClassId(), name())
    is Symbol.TopLevelMember -> CallableId(packageName.asFqName(), name())
    is Symbol.Constructor -> CallableId(classifier.asClassId(), SpecialNames.INIT)
}

public operator fun FirQualifiedAccessExpression.get(param: Symbol.Parameter): FirExpression? = when (param) {
    is Symbol.DispatchReceiverParameter -> dispatchReceiver
    is Symbol.ExtensionReceiverParameter -> extensionReceiver
    is Symbol.ContextParameter -> contextArguments[param.indexInContextParameters]
    is Symbol.ValueParameter -> if (this is FirFunctionCall) arguments[param.indexInValueParameters] else null
}

public operator fun FirQualifiedAccessExpression.get(param: Symbol.TypeParameter): FirTypeProjection {
    return typeArguments[param.index]
}

public operator fun ConeKotlinType.get(param: Symbol.TypeParameter): ConeTypeProjection {
    return typeArguments[param.index]
}


public operator fun IrMemberAccessExpression<*>.ValueArgumentsList.get(param: Symbol.Parameter): IrExpression? {
    return get(param.index)
}

public operator fun IrMemberAccessExpression<*>.ValueArgumentsList.set(param: Symbol.Parameter, value: IrExpression?) {
    set(param.index, value)
}

public operator fun IrMemberAccessExpression<*>.get(param: Symbol.TypeParameter): IrType? {
    return typeArguments[param.index]
}

public operator fun IrMemberAccessExpression<*>.set(param: Symbol.TypeParameter, value: IrType?) {
    typeArguments[param.index] = value
}

public operator fun IrTypeParametersContainer.get(param: Symbol.TypeParameter): IrTypeParameter {
    return typeParameters[param.index]
}

public operator fun IrSimpleType.get(param: Symbol.TypeParameter): IrTypeArgument {
    return arguments[param.index]
}

public operator fun IrType.get(param: Symbol.TypeParameter): IrTypeArgument? {
    return (this as? IrSimpleType)?.get(param)
}

internal fun ClassId.toClassifier() = Symbol.Classifier(
    NameSegments(packageFqName.pathSegments().map { it.asString() }),
    NameSegments(relativeClassName.pathSegments().map { it.asString() })
)