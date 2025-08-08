package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AnnotationGeneratorTest {

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

        val result = AnnotationGenerator.generateClass(annotation, emptySet())

        assertEquals(
            """
class test_package_TestAnnotation_Spec() : Symbol.Annotation<test_package_TestAnnotation_Spec, test_package_TestAnnotation_Spec.Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("TestAnnotation"),
) {
    public inner class Arguments() : Symbol.Annotation.Arguments<test_package_TestAnnotation_Spec, Arguments> {
        override val annotation: test_package_TestAnnotation_Spec get() = this@test_package_TestAnnotation_Spec
    }
    
    override fun produceArguments(producer: AnnotationArgumentProducer): test_package_TestAnnotation_Spec.Arguments = Arguments()
    
    public fun createArguments(): test_package_TestAnnotation_Spec.Arguments = Arguments()
}

        """.trimIndent(),
            result
        )
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

        val result = AnnotationGenerator.generateClass(annotation, emptySet())

        // Check parameter declarations
        assertEquals(
            """
class test_package_TestAnnotation_Spec() : Symbol.Annotation<test_package_TestAnnotation_Spec, test_package_TestAnnotation_Spec.Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("TestAnnotation"),
) {
    public val stringParam: AnnotationParameter<AnnotationParameterType.String> by lazy {
        AnnotationParameter("stringParam", AnnotationParameterType.String)
    }
    
    public val intParam: AnnotationParameter<AnnotationParameterType.Int> by lazy {
        AnnotationParameter("intParam", AnnotationParameterType.Int)
    }
    
    public val booleanParam: AnnotationParameter<AnnotationParameterType.Boolean> by lazy {
        AnnotationParameter("booleanParam", AnnotationParameterType.Boolean)
    }
    
    public inner class Arguments(
        public val stringParam: AnnotationArgument.String?,
        public val intParam: AnnotationArgument.Int?,
        public val booleanParam: AnnotationArgument.Boolean?,
    ) : Symbol.Annotation.Arguments<test_package_TestAnnotation_Spec, Arguments> {
        override val annotation: test_package_TestAnnotation_Spec get() = this@test_package_TestAnnotation_Spec
    }
    
    override fun produceArguments(producer: AnnotationArgumentProducer): test_package_TestAnnotation_Spec.Arguments = Arguments(
        producer.getArgument(this@test_package_TestAnnotation_Spec.stringParam),
        producer.getArgument(this@test_package_TestAnnotation_Spec.intParam),
        producer.getArgument(this@test_package_TestAnnotation_Spec.booleanParam),
    )
    
    public fun createArguments(
        stringParam: AnnotationArgument.String?,
        intParam: AnnotationArgument.Int?,
        booleanParam: AnnotationArgument.Boolean?,
    ): test_package_TestAnnotation_Spec.Arguments = Arguments(
        stringParam,
        intParam,
        booleanParam,
    )
}

        """.trimIndent(),
            result
        )
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

        val result = AnnotationGenerator.generateClass(annotation, emptySet())

        // Check parameter declarations
        assertEquals(
            """
class test_package_TestAnnotation_Spec() : Symbol.Annotation<test_package_TestAnnotation_Spec, test_package_TestAnnotation_Spec.Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("TestAnnotation"),
) {
    public val stringArray: AnnotationParameter<AnnotationParameterType.Array<AnnotationParameterType.String, AnnotationArgument.String>> by lazy {
        AnnotationParameter("stringArray", AnnotationParameterType.Array(elementType = AnnotationParameterType.String))
    }
    
    public inner class Arguments(
        public val stringArray: AnnotationArgument.Array<AnnotationArgument.String>?,
    ) : Symbol.Annotation.Arguments<test_package_TestAnnotation_Spec, Arguments> {
        override val annotation: test_package_TestAnnotation_Spec get() = this@test_package_TestAnnotation_Spec
    }
    
    override fun produceArguments(producer: AnnotationArgumentProducer): test_package_TestAnnotation_Spec.Arguments = Arguments(
        producer.getArgument(this@test_package_TestAnnotation_Spec.stringArray),
    )
    
    public fun createArguments(
        stringArray: AnnotationArgument.Array<AnnotationArgument.String>?,
    ): test_package_TestAnnotation_Spec.Arguments = Arguments(
        stringArray,
    )
}

        """.trimIndent(),
            result
        )
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

        val result = AnnotationGenerator.generateClass(annotation, emptySet())

        // Check parameter declarations
        assertEquals(
            """
class test_package_TestAnnotation_Spec() : Symbol.Annotation<test_package_TestAnnotation_Spec, test_package_TestAnnotation_Spec.Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("TestAnnotation"),
) {
    public val enumParam: AnnotationParameter<AnnotationParameterType.Enum> by lazy {
        AnnotationParameter("enumParam", AnnotationParameterType.Enum(enumClass = Classifier(packageName = NameSegments("test", "package"), classNames = NameSegments("TestEnum"))))
    }
    
    public inner class Arguments(
        public val enumParam: AnnotationArgument.EnumEntry?,
    ) : Symbol.Annotation.Arguments<test_package_TestAnnotation_Spec, Arguments> {
        override val annotation: test_package_TestAnnotation_Spec get() = this@test_package_TestAnnotation_Spec
    }
    
    override fun produceArguments(producer: AnnotationArgumentProducer): test_package_TestAnnotation_Spec.Arguments = Arguments(
        producer.getArgument(this@test_package_TestAnnotation_Spec.enumParam),
    )
    
    public fun createArguments(
        enumParam: AnnotationArgument.EnumEntry?,
    ): test_package_TestAnnotation_Spec.Arguments = Arguments(
        enumParam,
    )
}

        """.trimIndent(),
            result
        )
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

        val result = AnnotationGenerator.generateClass(annotation, emptySet())

        // Check parameter declarations
        assertEquals(
            """
class test_package_TestAnnotation_Spec() : Symbol.Annotation<test_package_TestAnnotation_Spec, test_package_TestAnnotation_Spec.Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("TestAnnotation"),
) {
    public val classParam: AnnotationParameter<AnnotationParameterType.KClass> by lazy {
        AnnotationParameter("classParam", AnnotationParameterType.KClass)
    }
    
    public inner class Arguments(
        public val classParam: AnnotationArgument.KClass?,
    ) : Symbol.Annotation.Arguments<test_package_TestAnnotation_Spec, Arguments> {
        override val annotation: test_package_TestAnnotation_Spec get() = this@test_package_TestAnnotation_Spec
    }
    
    override fun produceArguments(producer: AnnotationArgumentProducer): test_package_TestAnnotation_Spec.Arguments = Arguments(
        producer.getArgument(this@test_package_TestAnnotation_Spec.classParam),
    )
    
    public fun createArguments(
        classParam: AnnotationArgument.KClass?,
    ): test_package_TestAnnotation_Spec.Arguments = Arguments(
        classParam,
    )
}

        """.trimIndent(),
            result
        )
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

        val result = AnnotationGenerator.generateClass(annotation, emptySet())

        // Check parameter declarations
        assertEquals(
            """
class test_package_TestAnnotation_Spec() : Symbol.Annotation<test_package_TestAnnotation_Spec, test_package_TestAnnotation_Spec.Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("TestAnnotation"),
) {
    public val nestedAnnotation: AnnotationParameter<AnnotationParameterType.Annotation<test_package_NestedAnnotation_Spec, test_package_NestedAnnotation_Spec.Arguments>> by lazy {
        AnnotationParameter("nestedAnnotation", AnnotationParameterType.Annotation(annotationClass = test_package_NestedAnnotation))
    }
    
    public inner class Arguments(
        public val nestedAnnotation: AnnotationArgument.Annotation<test_package_NestedAnnotation_Spec, test_package_NestedAnnotation_Spec.Arguments>?,
    ) : Symbol.Annotation.Arguments<test_package_TestAnnotation_Spec, Arguments> {
        override val annotation: test_package_TestAnnotation_Spec get() = this@test_package_TestAnnotation_Spec
    }
    
    override fun produceArguments(producer: AnnotationArgumentProducer): test_package_TestAnnotation_Spec.Arguments = Arguments(
        producer.getArgument(this@test_package_TestAnnotation_Spec.nestedAnnotation),
    )
    
    public fun createArguments(
        nestedAnnotation: AnnotationArgument.Annotation<test_package_NestedAnnotation_Spec, test_package_NestedAnnotation_Spec.Arguments>?,
    ): test_package_TestAnnotation_Spec.Arguments = Arguments(
        nestedAnnotation,
    )
}

        """.trimIndent(),
            result
        )
    }

    @Test
    fun testAnnotationTypeConstructor() {
        // Test primitive type
        val stringType = AnnotationParameterType.Primitive.STRING
        assertEquals("AnnotationParameterType.String", AnnotationGenerator.annotationParameterTypeConstructor(stringType, emptySet()))

        // Test array type
        val arrayType = AnnotationParameterType.Array(AnnotationParameterType.Primitive.INT)
        assertEquals("AnnotationParameterType.Array(elementType = AnnotationParameterType.Int)", AnnotationGenerator.annotationParameterTypeConstructor(arrayType, emptySet()))

        // Test enum type
        val enumClass = InternalName.Classifier(listOf("test"), listOf("TestEnum"))
        val enumType = AnnotationParameterType.Enum(enumClass)
        assertEquals("AnnotationParameterType.Enum(enumClass = Classifier(packageName = NameSegments(\"test\"), classNames = NameSegments(\"TestEnum\")))", AnnotationGenerator.annotationParameterTypeConstructor(enumType, emptySet()))

        // Test KClass type
        assertEquals("AnnotationParameterType.KClass", AnnotationGenerator.annotationParameterTypeConstructor(AnnotationParameterType.KClass, emptySet()))

        // Test annotation type
        val annotationClass = InternalName.Classifier(listOf("test"), listOf("TestAnnotation"))
        val annotationType = AnnotationParameterType.Annotation(annotationClass)
        assertEquals("AnnotationParameterType.Annotation(annotationClass = test_TestAnnotation)", AnnotationGenerator.annotationParameterTypeConstructor(annotationType, emptySet()))
    }

    @Test
    fun testAnnotationParameterType() {
        // Test primitive type
        val stringType = AnnotationParameterType.Primitive.STRING
        assertEquals("AnnotationParameterType.String", AnnotationGenerator.annotationParameterType(stringType))

        // Test array type
        val arrayType = AnnotationParameterType.Array(AnnotationParameterType.Primitive.INT)
        assertEquals("AnnotationParameterType.Array<AnnotationParameterType.Int, AnnotationArgument.Int>", AnnotationGenerator.annotationParameterType(arrayType))

        // Test enum type
        val enumClass = InternalName.Classifier(listOf("test"), listOf("TestEnum"))
        val enumType = AnnotationParameterType.Enum(enumClass)
        assertEquals("AnnotationParameterType.Enum", AnnotationGenerator.annotationParameterType(enumType))

        // Test KClass type
        assertEquals("AnnotationParameterType.KClass", AnnotationGenerator.annotationParameterType(AnnotationParameterType.KClass))

        // Test annotation type
        val annotationClass = InternalName.Classifier(listOf("test"), listOf("TestAnnotation"))
        val annotationType = AnnotationParameterType.Annotation(annotationClass)
        assertEquals("AnnotationParameterType.Annotation<test_TestAnnotation_Spec, test_TestAnnotation_Spec.Arguments>", AnnotationGenerator.annotationParameterType(annotationType))
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

        val generatedCode = AnnotationGenerator.generateClass(annotation, emptySet())

        assertEquals(
            """
class test_package_ComplexAnnotation_Spec() : Symbol.Annotation<test_package_ComplexAnnotation_Spec, test_package_ComplexAnnotation_Spec.Arguments>(
    packageName = NameSegments("test", "package"),
    classNames = NameSegments("ComplexAnnotation"),
) {
    public val stringParam: AnnotationParameter<AnnotationParameterType.String> by lazy {
        AnnotationParameter("stringParam", AnnotationParameterType.String)
    }
    
    public val intParam: AnnotationParameter<AnnotationParameterType.Int> by lazy {
        AnnotationParameter("intParam", AnnotationParameterType.Int)
    }
    
    public val booleanParam: AnnotationParameter<AnnotationParameterType.Boolean> by lazy {
        AnnotationParameter("booleanParam", AnnotationParameterType.Boolean)
    }
    
    public val stringArray: AnnotationParameter<AnnotationParameterType.Array<AnnotationParameterType.String, AnnotationArgument.String>> by lazy {
        AnnotationParameter("stringArray", AnnotationParameterType.Array(elementType = AnnotationParameterType.String))
    }
    
    public val enumParam: AnnotationParameter<AnnotationParameterType.Enum> by lazy {
        AnnotationParameter("enumParam", AnnotationParameterType.Enum(enumClass = Classifier(packageName = NameSegments("test", "package"), classNames = NameSegments("TestEnum"))))
    }
    
    public val classParam: AnnotationParameter<AnnotationParameterType.KClass> by lazy {
        AnnotationParameter("classParam", AnnotationParameterType.KClass)
    }
    
    public val nestedAnnotation: AnnotationParameter<AnnotationParameterType.Annotation<test_package_NestedAnnotation_Spec, test_package_NestedAnnotation_Spec.Arguments>> by lazy {
        AnnotationParameter("nestedAnnotation", AnnotationParameterType.Annotation(annotationClass = test_package_NestedAnnotation))
    }
    
    public inner class Arguments(
        public val stringParam: AnnotationArgument.String?,
        public val intParam: AnnotationArgument.Int?,
        public val booleanParam: AnnotationArgument.Boolean?,
        public val stringArray: AnnotationArgument.Array<AnnotationArgument.String>?,
        public val enumParam: AnnotationArgument.EnumEntry?,
        public val classParam: AnnotationArgument.KClass?,
        public val nestedAnnotation: AnnotationArgument.Annotation<test_package_NestedAnnotation_Spec, test_package_NestedAnnotation_Spec.Arguments>?,
    ) : Symbol.Annotation.Arguments<test_package_ComplexAnnotation_Spec, Arguments> {
        override val annotation: test_package_ComplexAnnotation_Spec get() = this@test_package_ComplexAnnotation_Spec
    }
    
    override fun produceArguments(producer: AnnotationArgumentProducer): test_package_ComplexAnnotation_Spec.Arguments = Arguments(
        producer.getArgument(this@test_package_ComplexAnnotation_Spec.stringParam),
        producer.getArgument(this@test_package_ComplexAnnotation_Spec.intParam),
        producer.getArgument(this@test_package_ComplexAnnotation_Spec.booleanParam),
        producer.getArgument(this@test_package_ComplexAnnotation_Spec.stringArray),
        producer.getArgument(this@test_package_ComplexAnnotation_Spec.enumParam),
        producer.getArgument(this@test_package_ComplexAnnotation_Spec.classParam),
        producer.getArgument(this@test_package_ComplexAnnotation_Spec.nestedAnnotation),
    )
    
    public fun createArguments(
        stringParam: AnnotationArgument.String?,
        intParam: AnnotationArgument.Int?,
        booleanParam: AnnotationArgument.Boolean?,
        stringArray: AnnotationArgument.Array<AnnotationArgument.String>?,
        enumParam: AnnotationArgument.EnumEntry?,
        classParam: AnnotationArgument.KClass?,
        nestedAnnotation: AnnotationArgument.Annotation<test_package_NestedAnnotation_Spec, test_package_NestedAnnotation_Spec.Arguments>?,
    ): test_package_ComplexAnnotation_Spec.Arguments = Arguments(
        stringParam,
        intParam,
        booleanParam,
        stringArray,
        enumParam,
        classParam,
        nestedAnnotation,
    )
}

        """.trimIndent(),
            generatedCode
        )
    }
}