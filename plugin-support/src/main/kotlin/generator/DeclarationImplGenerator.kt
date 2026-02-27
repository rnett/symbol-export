package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalDeclaration

/**
 * Configures the [TypeSpec] for symbols using [dev.rnett.symbolexport.symbol.v2.Declaration].
 *
 * This generator adds the appropriate superclass and constructor parameters to the symbol object so that it implements `HasDeclaration<>` with the Declaration corresponding to the given symbol.
 * To fully represent the declaration, it likely will need to generate an object with parameters, type params, etc.
 */
internal interface DeclarationImplGenerator {
    fun addDeclarationInstance(builder: TypeSpec.Builder, objName: ClassName, declaration: InternalDeclaration)

    companion object : DeclarationImplGenerator {
        const val DECLARATION = "__declaration"
        const val INSTANCE = "__instance"

        override fun addDeclarationInstance(builder: TypeSpec.Builder, objName: ClassName, declaration: InternalDeclaration) {
            val declarationType = when (declaration) {
                is InternalDeclaration.Classifier if declaration.annotationInfo != null -> Names.AnnotationDeclaration
                is InternalDeclaration.Classifier if declaration.isObject -> Names.ObjectDeclaration
                is InternalDeclaration.Classifier -> Names.ClassDeclaration
                is InternalDeclaration.Constructor -> Names.ConstructorDeclaration
                is InternalDeclaration.SimpleFunction -> Names.SimpleFunctionDeclaration
                is InternalDeclaration.Property -> Names.PropertyDeclaration
            }

            val declarationObjBuilder = TypeSpec.objectBuilder(DECLARATION)
                .superclass(declarationType)
                .addSuperclassConstructorParameter("%L", SymbolInstanceGenerator.symbolInstance(declaration.symbol))

            context(declarationObjBuilder, objName) {
                buildDeclaration(declaration, builder)
            }

            builder.addType(declarationObjBuilder.build())
            builder.superclass(Names.HasDeclaration.parameterizedBy(declarationType))
            builder.addSuperclassConstructorParameter(DECLARATION)
        }

        context(builder: TypeSpec.Builder, objName: ClassName)
        private fun buildDeclaration(declaration: InternalDeclaration, parent: TypeSpec.Builder) {
            if (declaration is InternalDeclaration.Classifier) {
                if (declaration.annotationInfo != null) {
                    buildAnnotation(declaration, parent)
                }
            }
        }

        context(builder: TypeSpec.Builder, objName: ClassName)
        private fun buildAnnotation(declaration: InternalDeclaration.Classifier, parent: TypeSpec.Builder) {
            val info = declaration.annotationInfo!!

            val instanceClass = TypeSpec.classBuilder(INSTANCE)
            val ctor = FunSpec.constructorBuilder()
                .addParameter("arguments", Names.AnnotationDeclaration_ArgumentsMap)
            instanceClass.superclass(Names.AnnotationDeclaration_Instance)
            instanceClass.addSuperclassConstructorParameter("arguments")
            instanceClass.primaryConstructor(ctor.build())

            info.params.sortedBy { it.index }.forEach {
                val argType = it.type.argumentType()
                val paramType = it.type.parameterType()
                instanceClass.addProperty(
                    PropertySpec.builder(it.name, argType.makeNullable())
                        .getter(FunSpec.getterBuilder().addCode("return arguments.getForName(%S) as %T?", it.name, argType).build())
                        .build()
                )
                builder.addProperty(
                    PropertySpec.builder(it.name, Names.AnnotationParameter.parameterizedBy(paramType))
                        .initializer("%T(%S, %L, %L)", Names.AnnotationParameter, it.name, it.index, it.type.parameterTypeInstance())
                        .build()
                )
            }

            instanceClass.addProperty(
                PropertySpec.builder("annotation", objName.nestedClass(DECLARATION), KModifier.OVERRIDE)
                    .initializer("%T", objName.nestedClass(DECLARATION))
                    .build()
            )

            builder.addProperty(
                PropertySpec.builder("parameters", Names.Types.ParameterList, KModifier.OVERRIDE)
                    .initializer("listOf(%L)", info.params.sortedBy { it.index }.joinToString(",") { it.name })
                    .build()
            )

            builder.addFunction(
                FunSpec.builder("produceInstance")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("arguments", Names.AnnotationDeclaration_ArgumentsMap)
                    .returns(objName.nestedClass(INSTANCE))
                    .addCode("return %T(arguments)", objName.nestedClass(INSTANCE))
                    .build()
            )

            parent.addType(instanceClass.build())
        }
    }
}

private fun TypeName.makeNullable(): TypeName = this.copy(nullable = true)

/**
 * Maps to a type name of AnnotationArgument
 */
private fun AnnotationParameterType.argumentType(): TypeName = when (this) {
    AnnotationParameterType.KClass -> Names.AnnotationArgument.nestedClass("KClass")
    is AnnotationParameterType.Enum -> Names.AnnotationArgument.nestedClass("EnumEntry")
    is AnnotationParameterType.Annotation -> Names.AnnotationArgument.nestedClass("Annotation").parameterizedBy(STAR, STAR)
    is AnnotationParameterType.Array -> Names.AnnotationArgument.nestedClass("Array").parameterizedBy(elementType.argumentType())
    is AnnotationParameterType.Primitive -> when (this) {
        AnnotationParameterType.Primitive.STRING -> Names.AnnotationArgument.nestedClass("String")
        AnnotationParameterType.Primitive.BOOLEAN -> Names.AnnotationArgument.nestedClass("Boolean")
        AnnotationParameterType.Primitive.INT -> Names.AnnotationArgument.nestedClass("Int")
        AnnotationParameterType.Primitive.FLOAT -> Names.AnnotationArgument.nestedClass("Float")
        AnnotationParameterType.Primitive.LONG -> Names.AnnotationArgument.nestedClass("Long")
        AnnotationParameterType.Primitive.DOUBLE -> Names.AnnotationArgument.nestedClass("Double")
        AnnotationParameterType.Primitive.CHAR -> Names.AnnotationArgument.nestedClass("Char")
        AnnotationParameterType.Primitive.BYTE -> Names.AnnotationArgument.nestedClass("Byte")
        AnnotationParameterType.Primitive.SHORT -> Names.AnnotationArgument.nestedClass("Short")
    }
}

/**
 * Maps to a type name of AnnotationParameterType
 */
private fun AnnotationParameterType.parameterType(): TypeName = when (this) {
    AnnotationParameterType.KClass -> Names.AnnotationParameterType.nestedClass("KClass")
    is AnnotationParameterType.Enum -> Names.AnnotationParameterType.nestedClass("Enum")
    is AnnotationParameterType.Annotation -> Names.AnnotationParameterType.nestedClass("Annotation").parameterizedBy(STAR, STAR)
    is AnnotationParameterType.Array -> Names.AnnotationParameterType.nestedClass("Array").parameterizedBy(elementType.parameterType(), elementType.argumentType())
    is AnnotationParameterType.Primitive -> when (this) {
        AnnotationParameterType.Primitive.STRING -> Names.AnnotationParameterType.nestedClass("String")
        AnnotationParameterType.Primitive.BOOLEAN -> Names.AnnotationParameterType.nestedClass("Boolean")
        AnnotationParameterType.Primitive.INT -> Names.AnnotationParameterType.nestedClass("Int")
        AnnotationParameterType.Primitive.FLOAT -> Names.AnnotationParameterType.nestedClass("Float")
        AnnotationParameterType.Primitive.LONG -> Names.AnnotationParameterType.nestedClass("Long")
        AnnotationParameterType.Primitive.DOUBLE -> Names.AnnotationParameterType.nestedClass("Double")
        AnnotationParameterType.Primitive.CHAR -> Names.AnnotationParameterType.nestedClass("Char")
        AnnotationParameterType.Primitive.BYTE -> Names.AnnotationParameterType.nestedClass("Byte")
        AnnotationParameterType.Primitive.SHORT -> Names.AnnotationParameterType.nestedClass("Short")
    }
}

//TODO need to update annotation types for v2

/**
 * Creates an instance of AnnotationParameterType for this
 */
private fun AnnotationParameterType.parameterTypeInstance(): CodeBlock = when (this) {
    AnnotationParameterType.KClass -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("KClass"))
    is AnnotationParameterType.Enum -> CodeBlock.of("%T(%L)", Names.AnnotationParameterType.nestedClass("Enum"), SymbolInstanceGenerator.symbolInstance(enumClass.symbol))
    is AnnotationParameterType.Annotation -> CodeBlock.of("%T(%L)", Names.AnnotationParameterType.nestedClass("Annotation"), SymbolInstanceGenerator.symbolInstance(annotationClass.symbol))
    is AnnotationParameterType.Array -> CodeBlock.of("%T(%L)", Names.AnnotationParameterType.nestedClass("Array"), elementType.parameterTypeInstance())
    is AnnotationParameterType.Primitive -> when (this) {
        AnnotationParameterType.Primitive.STRING -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("String"))
        AnnotationParameterType.Primitive.BOOLEAN -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Boolean"))
        AnnotationParameterType.Primitive.INT -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Int"))
        AnnotationParameterType.Primitive.FLOAT -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Float"))
        AnnotationParameterType.Primitive.LONG -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Long"))
        AnnotationParameterType.Primitive.DOUBLE -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Double"))
        AnnotationParameterType.Primitive.CHAR -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Char"))
        AnnotationParameterType.Primitive.BYTE -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Byte"))
        AnnotationParameterType.Primitive.SHORT -> CodeBlock.of("%T", Names.AnnotationParameterType.nestedClass("Short"))
    }
}
