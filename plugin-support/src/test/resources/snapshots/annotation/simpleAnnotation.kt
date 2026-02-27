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
    public object MyAnnotation : HasDeclaration<AnnotationDeclaration>(__declaration) {
        public class __instance(
            arguments: AnnotationDeclaration.ArgumentsMap,
        ) : AnnotationDeclaration.Instance(arguments) {
            public val name: AnnotationArgument.String?
                get() = arguments.getForName("name") as AnnotationArgument.String?

            public val age: AnnotationArgument.Int?
                get() = arguments.getForName("age") as AnnotationArgument.Int?

            override val `annotation`: __declaration = __declaration
        }

        public object __declaration : AnnotationDeclaration(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("MyAnnotation")))) {
            public val name: AnnotationParameter<AnnotationParameterType.String> =
                AnnotationParameter("name", 0, AnnotationParameterType.String)

            public val age: AnnotationParameter<AnnotationParameterType.Int> =
                AnnotationParameter("age", 1, AnnotationParameterType.Int)

            override val parameters: List<AnnotationParameter<*>> = listOf(name, age)

            override fun produceInstance(arguments: AnnotationDeclaration.ArgumentsMap): __instance = __instance(arguments)
        }
    }
}