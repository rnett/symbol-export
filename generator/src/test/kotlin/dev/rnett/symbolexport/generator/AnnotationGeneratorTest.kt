package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnnotationGeneratorTest {

    /**
     * Helper method to verify the complete generated code against an expected code block.
     * This implements a snapshot-like test for code generation.
     */
    private fun assertGeneratedCode(expected: String, actual: String) {
        val normalizedExpected = expected.trim().replace("\r\n", "\n")
        val normalizedActual = actual.trim().replace("\r\n", "\n")

        assertEquals(normalizedExpected, normalizedActual, "Generated code does not match expected code")
    }

    @Test
    fun testAnnotationClassName() {
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = emptyMap()
        )

        val result = AnnotationGenerator.annotationClassName(annotation)
        assertEquals("test_package_TestAnnotation_Spec", result)
    }

    @Test
    fun testGenerateClassWithNoParameters() {
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = emptyMap()
        )

        val result = AnnotationGenerator.generateClass(annotation)

        println("ACTUAL OUTPUT:")
        println(result)

        val expectedCode = """
class test_package_TestAnnotation_Spec private constructor() : Symbol.Annotation<test_package_TestAnnotation_Spec, Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("TestAnnotation"),
) {
    public inner class Arguments private constructor(producer: AnnotationArgumentProducer) : Symbol.Annotation.Arguments<test_package_TestAnnotation_Spec, Arguments>() {
        override val annotation: test_package_TestAnnotation_Spec get() = this@test_package_TestAnnotation_Spec
    }
    override fun produceArguments(producer: AnnotationArgumentProducer): Arguments = Arguments(producer)
}
        """.trimIndent()

        assertGeneratedCode(expectedCode, result)
    }

    @Test
    fun testGenerateClassWithPrimitiveParameters() {
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "stringParam" to AnnotationParameterType.Primitive.STRING,
                "intParam" to AnnotationParameterType.Primitive.INT,
                "booleanParam" to AnnotationParameterType.Primitive.BOOLEAN
            )
        )

        val result = AnnotationGenerator.generateClass(annotation)

        // Check parameter declarations
        assertTrue(result.contains("public val stringParam: AnnotationParameter by lazy { AnnotationParameter(\"stringParam\", AnnotationParameterType.String) }"))
        assertTrue(result.contains("public val intParam: AnnotationParameter by lazy { AnnotationParameter(\"intParam\", AnnotationParameterType.Int) }"))
        assertTrue(result.contains("public val booleanParam: AnnotationParameter by lazy { AnnotationParameter(\"booleanParam\", AnnotationParameterType.Boolean) }"))

        // Check Arguments class parameter declarations
        assertTrue(result.contains("public val stringParam: AnnotationArgument.String = producer.getArgument(this@test_package_TestAnnotation_Spec.stringParam)"))
        assertTrue(result.contains("public val intParam: AnnotationArgument.Int = producer.getArgument(this@test_package_TestAnnotation_Spec.intParam)"))
        assertTrue(result.contains("public val booleanParam: AnnotationArgument.Boolean = producer.getArgument(this@test_package_TestAnnotation_Spec.booleanParam)"))
    }

    @Test
    fun testGenerateClassWithArrayParameter() {
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "stringArray" to AnnotationParameterType.Array(AnnotationParameterType.Primitive.STRING)
            )
        )

        val result = AnnotationGenerator.generateClass(annotation)

        // Check parameter declaration
        assertTrue(result.contains("public val stringArray: AnnotationParameter by lazy { AnnotationParameter(\"stringArray\", AnnotationParameterType.Array(elementType = AnnotationParameterType.String)) }"))

        // Check Arguments class parameter declaration
        assertTrue(result.contains("public val stringArray: AnnotationArgument.Array<AnnotationArgument.String> = producer.getArgument(this@test_package_TestAnnotation_Spec.stringArray)"))
    }

    @Test
    fun testGenerateClassWithEnumParameter() {
        val enumClass = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestEnum")
        )

        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "enumParam" to AnnotationParameterType.Enum(enumClass)
            )
        )

        val result = AnnotationGenerator.generateClass(annotation)

        // Check parameter declaration
        assertTrue(result.contains("public val enumParam: AnnotationParameter by lazy { AnnotationParameter(\"enumParam\", AnnotationParameterType.Enum(enumClass = test_package_TestEnum)) }"))

        // Check Arguments class parameter declaration
        assertTrue(result.contains("public val enumParam: AnnotationArgument.EnumEntry = producer.getArgument(this@test_package_TestAnnotation_Spec.enumParam)"))
    }

    @Test
    fun testGenerateClassWithKClassParameter() {
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "classParam" to AnnotationParameterType.KClass
            )
        )

        val result = AnnotationGenerator.generateClass(annotation)

        // Check parameter declaration
        assertTrue(result.contains("public val classParam: AnnotationParameter by lazy { AnnotationParameter(\"classParam\", AnnotationParameterType.KClass) }"))

        // Check Arguments class parameter declaration
        assertTrue(result.contains("public val classParam: AnnotationArgument.KClass = producer.getArgument(this@test_package_TestAnnotation_Spec.classParam)"))
    }

    @Test
    fun testGenerateClassWithNestedAnnotationParameter() {
        val nestedAnnotationClass = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("NestedAnnotation")
        )

        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "nestedAnnotation" to AnnotationParameterType.Annotation(nestedAnnotationClass)
            )
        )

        val result = AnnotationGenerator.generateClass(annotation)

        // Check parameter declaration
        assertTrue(result.contains("public val nestedAnnotation: AnnotationParameter by lazy { AnnotationParameter(\"nestedAnnotation\", AnnotationParameterType.Annotation<test_package_NestedAnnotation_Spec>(annotationClass = test_package_NestedAnnotation)) }"))

        // Check Arguments class parameter declaration
        assertTrue(result.contains("public val nestedAnnotation: AnnotationArgument.Annotation<test_package_NestedAnnotation_Spec.Arguments> = producer.getArgument(this@test_package_TestAnnotation_Spec.nestedAnnotation)"))
    }

    @Test
    fun testAnnotationTypeConstructor() {
        // Test primitive type
        val stringType = AnnotationParameterType.Primitive.STRING
        assertEquals("AnnotationParameterType.String", AnnotationGenerator.annotationTypeConstructor(stringType))

        // Test array type
        val arrayType = AnnotationParameterType.Array(AnnotationParameterType.Primitive.INT)
        assertEquals("AnnotationParameterType.Array(elementType = AnnotationParameterType.Int)", AnnotationGenerator.annotationTypeConstructor(arrayType))

        // Test enum type
        val enumClass = InternalName.Classifier(listOf("test"), listOf("TestEnum"))
        val enumType = AnnotationParameterType.Enum(enumClass)
        assertEquals("AnnotationParameterType.Enum(enumClass = test_TestEnum)", AnnotationGenerator.annotationTypeConstructor(enumType))

        // Test KClass type
        assertEquals("AnnotationParameterType.KClass", AnnotationGenerator.annotationTypeConstructor(AnnotationParameterType.KClass))

        // Test annotation type
        val annotationClass = InternalName.Classifier(listOf("test"), listOf("TestAnnotation"))
        val annotationType = AnnotationParameterType.Annotation(annotationClass)
        assertEquals("AnnotationParameterType.Annotation<test_TestAnnotation_Spec>(annotationClass = test_TestAnnotation)", AnnotationGenerator.annotationTypeConstructor(annotationType))
    }

    @Test
    fun testAnnotationValueType() {
        // Test primitive type
        val stringType = AnnotationParameterType.Primitive.STRING
        assertEquals("AnnotationArgument.String", AnnotationGenerator.annotationValueType(stringType))

        // Test array type
        val arrayType = AnnotationParameterType.Array(AnnotationParameterType.Primitive.INT)
        assertEquals("AnnotationArgument.Array<AnnotationArgument.Int>", AnnotationGenerator.annotationValueType(arrayType))

        // Test enum type
        val enumClass = InternalName.Classifier(listOf("test"), listOf("TestEnum"))
        val enumType = AnnotationParameterType.Enum(enumClass)
        assertEquals("AnnotationArgument.EnumEntry", AnnotationGenerator.annotationValueType(enumType))

        // Test KClass type
        assertEquals("AnnotationArgument.KClass", AnnotationGenerator.annotationValueType(AnnotationParameterType.KClass))

        // Test annotation type
        val annotationClass = InternalName.Classifier(listOf("test"), listOf("TestAnnotation"))
        val annotationType = AnnotationParameterType.Annotation(annotationClass)
        assertEquals("AnnotationArgument.Annotation<test_TestAnnotation_Spec.Arguments>", AnnotationGenerator.annotationValueType(annotationType))
    }

    @Test
    fun testComplexAnnotationGenerationSnapshot() {
        // Create a complex annotation with various parameter types
        val enumClass = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestEnum")
        )

        val nestedAnnotationClass = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("NestedAnnotation")
        )

        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("ComplexAnnotation"),
            parameters = mapOf(
                "stringParam" to AnnotationParameterType.Primitive.STRING,
                "intParam" to AnnotationParameterType.Primitive.INT,
                "booleanParam" to AnnotationParameterType.Primitive.BOOLEAN,
                "stringArray" to AnnotationParameterType.Array(AnnotationParameterType.Primitive.STRING),
                "enumParam" to AnnotationParameterType.Enum(enumClass),
                "classParam" to AnnotationParameterType.KClass,
                "nestedAnnotation" to AnnotationParameterType.Annotation(nestedAnnotationClass)
            )
        )

        val generatedCode = AnnotationGenerator.generateClass(annotation)

        val expectedCode = """
class test_package_ComplexAnnotation_Spec private constructor() : Symbol.Annotation<test_package_ComplexAnnotation_Spec, Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("ComplexAnnotation"),
) {
    public val stringParam: AnnotationParameter by lazy { AnnotationParameter("stringParam", AnnotationParameterType.String) }
    
    public val intParam: AnnotationParameter by lazy { AnnotationParameter("intParam", AnnotationParameterType.Int) }
    
    public val booleanParam: AnnotationParameter by lazy { AnnotationParameter("booleanParam", AnnotationParameterType.Boolean) }
    
    public val stringArray: AnnotationParameter by lazy { AnnotationParameter("stringArray", AnnotationParameterType.Array(elementType = AnnotationParameterType.String)) }
    
    public val enumParam: AnnotationParameter by lazy { AnnotationParameter("enumParam", AnnotationParameterType.Enum(enumClass = test_package_TestEnum)) }
    
    public val classParam: AnnotationParameter by lazy { AnnotationParameter("classParam", AnnotationParameterType.KClass) }
    
    public val nestedAnnotation: AnnotationParameter by lazy { AnnotationParameter("nestedAnnotation", AnnotationParameterType.Annotation<test_package_NestedAnnotation_Spec>(annotationClass = test_package_NestedAnnotation)) }
    
    public inner class Arguments private constructor(producer: AnnotationArgumentProducer) : Symbol.Annotation.Arguments<test_package_ComplexAnnotation_Spec, Arguments>() {
        override val annotation: test_package_ComplexAnnotation_Spec get() = this@test_package_ComplexAnnotation_Spec
        
        public val stringParam: AnnotationArgument.String = producer.getArgument(this@test_package_ComplexAnnotation_Spec.stringParam)
        public val intParam: AnnotationArgument.Int = producer.getArgument(this@test_package_ComplexAnnotation_Spec.intParam)
        public val booleanParam: AnnotationArgument.Boolean = producer.getArgument(this@test_package_ComplexAnnotation_Spec.booleanParam)
        public val stringArray: AnnotationArgument.Array<AnnotationArgument.String> = producer.getArgument(this@test_package_ComplexAnnotation_Spec.stringArray)
        public val enumParam: AnnotationArgument.EnumEntry = producer.getArgument(this@test_package_ComplexAnnotation_Spec.enumParam)
        public val classParam: AnnotationArgument.KClass = producer.getArgument(this@test_package_ComplexAnnotation_Spec.classParam)
        public val nestedAnnotation: AnnotationArgument.Annotation<test_package_NestedAnnotation_Spec.Arguments> = producer.getArgument(this@test_package_ComplexAnnotation_Spec.nestedAnnotation)
    }
    override fun produceArguments(producer: AnnotationArgumentProducer): Arguments = Arguments(producer)
}
        """.trimIndent()

        assertGeneratedCode(expectedCode, generatedCode)
    }
}