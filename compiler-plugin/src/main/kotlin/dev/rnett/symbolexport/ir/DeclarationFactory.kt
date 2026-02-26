package dev.rnett.symbolexport.ir

import dev.rnett.kcp.development.utils.ir.ExperimentalIrHelpers
import dev.rnett.kcp.development.utils.ir.WithIrContext
import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalDeclaration
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrParameterKind
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.types.getPrimitiveType
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isKClass
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.isAccessor
import org.jetbrains.kotlin.ir.util.isAnnotation
import org.jetbrains.kotlin.ir.util.isAnnotationClass
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isPrimitiveArray
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.render

@OptIn(ExperimentalIrHelpers::class)
class DeclarationFactory(override val context: IrPluginContext) : WithIrContext {
    val symbolFactory = SymbolFactory(context)

    fun classDeclaration(declaration: IrClass): InternalDeclaration.Classifier {
        return InternalDeclaration.Classifier(
            symbolFactory.classSymbol(declaration),
            declaration.typeParameters.map { it.toModel() },
            if (declaration.isAnnotationClass) annotationInfo(declaration) else null,
            if (declaration.isEnumClass) enumInfo(declaration) else null,
            declaration.modality.let { it == Modality.FINAL || it == Modality.OPEN },
            declaration.kind == ClassKind.OBJECT,
        )
    }

    fun functionDeclaration(declaration: IrFunction): InternalDeclaration.Function {
        return when (declaration) {
            is IrConstructor -> {
                InternalDeclaration.Constructor(
                    symbolFactory.functionSymbol(declaration),
                    declaration.parentAsClass.typeParameters.map { it.toModel() },
                    declaration.parameters.map { it.toModel() }
                )
            }

            is IrSimpleFunction -> functionDeclaration(declaration)
        }
    }

    fun functionDeclaration(declaration: IrSimpleFunction): InternalDeclaration.SimpleFunction {
        return InternalDeclaration.SimpleFunction(
            symbolFactory.functionSymbol(declaration),
            declaration.typeParameters.map { it.toModel() },
            declaration.parameters.map { it.toModel() },
            declaration.isAccessor
        )
    }

    fun propertyDeclaration(declaration: IrProperty): InternalDeclaration.Property {
        return InternalDeclaration.Property(
            symbolFactory.propertySymbol(declaration),
            declaration.getter?.let { functionDeclaration(it) } ?: error("No getter for property ${declaration.dump()}"),
            declaration.setter?.let { functionDeclaration(it) }
        )
    }

    private fun IrTypeParameter.toModel(): InternalDeclaration.TypeParameter =
        InternalDeclaration.TypeParameter(
            name = name.asString(),
            index = this.index,
        )

    private fun IrValueParameter.toModel(): InternalDeclaration.Parameter =
        InternalDeclaration.Parameter(
            name = name.asString(),
            index = this.indexInParameters,
            kind = when (kind) {
                IrParameterKind.DispatchReceiver -> InternalDeclaration.Parameter.Kind.DISPATCH
                IrParameterKind.Context -> InternalDeclaration.Parameter.Kind.CONTEXT
                IrParameterKind.ExtensionReceiver -> InternalDeclaration.Parameter.Kind.EXTENSION
                IrParameterKind.Regular -> InternalDeclaration.Parameter.Kind.VALUE
            }
        )

    private fun enumInfo(declaration: IrClass): InternalDeclaration.EnumInfo {
        return InternalDeclaration.EnumInfo(declaration.declarations.filterIsInstance<IrEnumEntry>().map { it.name.asString() })
    }

    private fun annotationInfo(declaration: IrClass): InternalDeclaration.AnnotationInfo {
        return InternalDeclaration.AnnotationInfo(
            declaration.primaryConstructor!!.parameters
                .map { it.toAnnotationParam() }
        )
    }

    private fun IrValueParameter.toAnnotationParam(): InternalDeclaration.AnnotationParameter {
        return InternalDeclaration.AnnotationParameter(
            name = name.asString(),
            index = this.indexInParameters,
            type = type.toAnnotationParamType()
        )
    }

    private fun IrType.toAnnotationParamType(): AnnotationParameterType {
        return when {
            isAnnotation() -> AnnotationParameterType.Annotation(classDeclaration(this.classOrFail.owner))
            isKClass() -> AnnotationParameterType.KClass
            getPrimitiveType() != null -> primitiveAnnotationType(getPrimitiveType()!!)
            isArray() || isPrimitiveArray() -> AnnotationParameterType.Array(this.getArrayElementType(builtIns).toAnnotationParamType())
            else -> error("Unsupported annotation parameter type ${type.render()}")
        }
    }

    private fun primitiveAnnotationType(primitiveType: PrimitiveType): AnnotationParameterType {
        return when (primitiveType) {
            PrimitiveType.BOOLEAN -> AnnotationParameterType.Primitive.BOOLEAN
            PrimitiveType.CHAR -> AnnotationParameterType.Primitive.CHAR
            PrimitiveType.BYTE -> AnnotationParameterType.Primitive.BYTE
            PrimitiveType.SHORT -> AnnotationParameterType.Primitive.SHORT
            PrimitiveType.INT -> AnnotationParameterType.Primitive.INT
            PrimitiveType.FLOAT -> AnnotationParameterType.Primitive.FLOAT
            PrimitiveType.LONG -> AnnotationParameterType.Primitive.LONG
            PrimitiveType.DOUBLE -> AnnotationParameterType.Primitive.DOUBLE
        }
    }

}
