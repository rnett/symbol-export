package com.example

import dev.rnett.symbolexport.symbol.`annotation`.AnnotationArgument
import dev.rnett.symbolexport.symbol.`annotation`.AnnotationParameter
import dev.rnett.symbolexport.symbol.`annotation`.AnnotationParameterType
import dev.rnett.symbolexport.symbol.v2.AnnotationDeclaration
import dev.rnett.symbolexport.symbol.v2.ClassSymbol
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName
import kotlin.collections.List

public object Symbols {
    public object ComplexAnnotation : HasDeclaration<AnnotationDeclaration>(__declaration) {
        public class __instance(
            arguments: AnnotationDeclaration.ArgumentsMap,
        ) : AnnotationDeclaration.Instance(arguments) {
            public val clazz: AnnotationArgument.KClass?
                get() = arguments.getForName("clazz") as AnnotationArgument.KClass?

            public val `enum`: AnnotationArgument.EnumEntry?
                get() = arguments.getForName("enum") as AnnotationArgument.EnumEntry?

            public val anno: AnnotationArgument.Annotation<*, *>?
                get() = arguments.getForName("anno") as AnnotationArgument.Annotation<*, *>?

            public val strings: AnnotationArgument.Array<AnnotationArgument.String>?
                get() = arguments.getForName("strings") as AnnotationArgument.Array<AnnotationArgument.String>?

            override val `annotation`: __declaration = __declaration
        }

        public object __declaration : AnnotationDeclaration(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("ComplexAnnotation")))) {
            public val clazz: AnnotationParameter<AnnotationParameterType.KClass> =
                AnnotationParameter("clazz", 0, AnnotationParameterType.KClass)

            public val `enum`: AnnotationParameter<AnnotationParameterType.Enum> =
                AnnotationParameter("enum", 1, AnnotationParameterType.Enum(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("MyEnum")))))

            public val anno: AnnotationParameter<AnnotationParameterType.Annotation<*, *>> =
                AnnotationParameter("anno", 2, AnnotationParameterType.Annotation(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("NestedAnno")))))

            public val strings:
                    AnnotationParameter<AnnotationParameterType.Array<AnnotationParameterType.String, AnnotationArgument.String>> =
                AnnotationParameter("strings", 3, AnnotationParameterType.Array(AnnotationParameterType.String))

            override val parameters: List<AnnotationParameter<*>> = listOf(clazz, enum, anno, strings)

            override fun produceInstance(arguments: AnnotationDeclaration.ArgumentsMap): __instance = __instance(arguments)
        }
    }
}