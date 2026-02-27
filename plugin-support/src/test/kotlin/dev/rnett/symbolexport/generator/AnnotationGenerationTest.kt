package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.SymbolTarget
import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalDeclaration
import dev.rnett.symbolexport.internal.InternalSymbol
import dev.rnett.symbolexport.postprocessor.TargetSymbol
import org.junit.jupiter.api.Test

class AnnotationGenerationTest : AbstractGeneratorSnapshotTest() {

    @Test
    fun testSimpleAnnotation() {
        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("MyAnnotation"))
        val declaration = InternalDeclaration.Classifier(
            symbol = symbol,
            typeParams = emptyList(),
            annotationInfo = InternalDeclaration.AnnotationInfo(
                listOf(
                    InternalDeclaration.AnnotationParameter("name", 0, AnnotationParameterType.Primitive.STRING),
                    InternalDeclaration.AnnotationParameter("age", 1, AnnotationParameterType.Primitive.INT)
                )
            ),
            enumInfo = null,
            isConcrete = true,
            isObject = false
        )
        val targetSymbol = TargetSymbol(symbol, declaration, setOf(SymbolTarget("jvm")))
        val builder = DeclarationGenerator.addDeclaration(targetSymbol, parentName)
        assertSnapshot(builder.build(), "annotation/simpleAnnotation")
    }

    @Test
    fun testComplexAnnotation() {
        val enumSymbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("MyEnum"))
        val enumDecl = InternalDeclaration.Classifier(enumSymbol, emptyList(), null, null, true, false)

        val nestedAnnoSymbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("NestedAnno"))
        val nestedAnnoDecl = InternalDeclaration.Classifier(nestedAnnoSymbol, emptyList(), null, null, true, false)

        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("ComplexAnnotation"))
        val declaration = InternalDeclaration.Classifier(
            symbol = symbol,
            typeParams = emptyList(),
            annotationInfo = InternalDeclaration.AnnotationInfo(
                listOf(
                    InternalDeclaration.AnnotationParameter("clazz", 0, AnnotationParameterType.KClass),
                    InternalDeclaration.AnnotationParameter("enum", 1, AnnotationParameterType.Enum(enumDecl)),
                    InternalDeclaration.AnnotationParameter("anno", 2, AnnotationParameterType.Annotation(nestedAnnoDecl)),
                    InternalDeclaration.AnnotationParameter("strings", 3, AnnotationParameterType.Array(AnnotationParameterType.Primitive.STRING))
                )
            ),
            enumInfo = null,
            isConcrete = true,
            isObject = false
        )
        val targetSymbol = TargetSymbol(symbol, declaration, setOf(SymbolTarget("jvm")))
        val builder = DeclarationGenerator.addDeclaration(targetSymbol, parentName)
        assertSnapshot(builder.build(), "annotation/complexAnnotation")
    }
}
