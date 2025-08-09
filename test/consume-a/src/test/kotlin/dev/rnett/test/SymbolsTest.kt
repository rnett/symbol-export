package dev.rnett.test

import dev.rnett.symbolexport.symbol.NameSegments
import dev.rnett.symbolexport.symbol.Symbol
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SymbolsTest {

    @Test
    fun testTopLevelFunction() {
        assertTrue(Symbols.dev_rnett_test_topLevelFunction in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelFunction"),
            Symbols.dev_rnett_test_topLevelFunction
        )
    }

    @Test
    fun testTopLevelProperty() {
        assertTrue(Symbols.dev_rnett_test_topLevelProperty in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "topLevelProperty"),
            Symbols.dev_rnett_test_topLevelProperty
        )
    }

    @Test
    fun testExposedClass() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass")
            ),
            Symbols.dev_rnett_test_ExposedClass
        )
    }

    @Test
    fun testExposedClassProp() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_prop in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.NamedClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass")
                ),
                name = "prop"
            ),
            Symbols.dev_rnett_test_ExposedClass_prop
        )
    }

    @Test
    fun testExposedClassCompanionExposedFun() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_Companion_exposedFun in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.NamedClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "Companion")
                ),
                name = "exposedFun"
            ),
            Symbols.dev_rnett_test_ExposedClass_Companion_exposedFun
        )
    }

    @Test
    fun testExposedClassExposedNestedClass() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass
        )
    }

    @Test
    fun testExposedClassExposedNestedClassExposedFun() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedFun in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.NamedClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "ExposedNestedClass")
                ),
                name = "exposedFun"
            ),
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedFun
        )
    }

    @Test
    fun testExposedClassExposedNestedClassExposedProperty() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedProperty in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.NamedClassifierMember(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass", "ExposedNestedClass")
                ),
                name = "exposedProperty"
            ),
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedProperty
        )
    }

    @Test
    fun testExposedClassInit() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_init in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.Constructor(
                classifier = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("ExposedClass")
                ),
            ),
            Symbols.dev_rnett_test_ExposedClass_init
        )
    }

    @Test
    fun testExposedClassInitParam() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_init_param in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ValueParameter(
                owner = Symbol.Constructor(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                ),
                index = 0,
                indexInValueParameters = 0,
                name = "param"
            ),
            Symbols.dev_rnett_test_ExposedClass_init_param
        )
    }

    @Test
    fun testExposedClassWithTypeParametersT() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withTypeParameters_T in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TypeParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withTypeParameters"
                ),
                index = 0,
                name = "T"
            ),
            Symbols.dev_rnett_test_ExposedClass_withTypeParameters_T
        )
    }

    @Test
    fun testExposedClassWithValueParametersT() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withValueParameters_t in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ValueParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withValueParameters"
                ),
                index = 1,
                indexInValueParameters = 0,
                name = "t"
            ),
            Symbols.dev_rnett_test_ExposedClass_withValueParameters_t
        )
    }

    @Test
    fun testExposedClassWithExtensionReceiverReceiver() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withExtensionReceiver_receiver in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ExtensionReceiverParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withExtensionReceiver"
                ),
                index = 1,
                name = "<receiver>"
            ),
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiver_receiver
        )
    }

    @Test
    fun testExposedClassWithExtensionReceiverAllThis() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverAll_this in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.DispatchReceiverParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withExtensionReceiverAll"
                ),
                index = 0,
                name = "<this>"
            ),
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverAll_this
        )
    }

    @Test
    fun testExposedClassWithExtensionReceiverAllReceiver() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverAll_receiver in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ExtensionReceiverParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withExtensionReceiverAll"
                ),
                index = 1,
                name = "<receiver>"
            ),
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverAll_receiver
        )
    }

    @Test
    fun testExposedClassWithContextParametersA() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withContextParameters_a in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ContextParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withContextParameters"
                ),
                index = 1,
                indexInContextParameters = 0,
                name = "a"
            ),
            Symbols.dev_rnett_test_ExposedClass_withContextParameters_a
        )
    }

    @Test
    fun testExposedClassWithExtensionReceiverJustExtensionReceiver() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverJustExtension_receiver in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.ExtensionReceiverParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withExtensionReceiverJustExtension"
                ),
                index = 1,
                name = "<receiver>"
            ),
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverJustExtension_receiver
        )
    }

    @Test
    fun testExposedClassWithExtensionReceiverJustDispatchThis() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverJustDispatch_this in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.DispatchReceiverParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withExtensionReceiverJustDispatch"
                ),
                index = 0,
                name = "<this>"
            ),
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverJustDispatch_this
        )
    }

    @Test
    fun testExposedClassWithDispatchThis() {
        assertTrue(Symbols.dev_rnett_test_ExposedClass_withDispatch_this in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.DispatchReceiverParameter(
                owner = Symbol.NamedClassifierMember(
                    classifier = Symbol.Classifier(
                        packageName = NameSegments("dev", "rnett", "test"),
                        classNames = NameSegments("ExposedClass")
                    ),
                    name = "withDispatch"
                ),
                index = 0,
                name = "<this>"
            ),
            Symbols.dev_rnett_test_ExposedClass_withDispatch_this
        )
    }

    @Test
    fun testWithValueParamsT() {
        assertTrue(Symbols.dev_rnett_test_WithValueParams_T in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TypeParameter(
                owner = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("WithValueParams")
                ),
                index = 0,
                name = "T"
            ),
            Symbols.dev_rnett_test_WithValueParams_T
        )
    }

    @Test
    fun testTestEnumA() {
        assertTrue(Symbols.dev_rnett_test_TestEnum_A in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.EnumEntry(
                enumClass = Symbol.Classifier(
                    packageName = NameSegments("dev", "rnett", "test"),
                    classNames = NameSegments("TestEnum")
                ),
                entryName = "A",
                entryOrdinal = 0
            ),
            Symbols.dev_rnett_test_TestEnum_A
        )
    }

    @Test
    fun testJvmOnlySymbol() {
        assertTrue(Symbols.JvmMain.dev_rnett_test_jvmOnly in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.TopLevelMember(packageName = NameSegments("dev", "rnett", "test"), name = "jvmOnly"),
            Symbols.JvmMain.dev_rnett_test_jvmOnly
        )
    }

    @Test
    fun testAllSymbols() {
        // Test that ALL_SYMBOLS contains exactly the expected symbols
        val expectedSymbols = setOf(
            Symbols.dev_rnett_test_topLevelFunction,
            Symbols.dev_rnett_test_topLevelProperty,
            Symbols.dev_rnett_test_ExposedClass,
            Symbols.dev_rnett_test_ExposedClass_prop,
            Symbols.dev_rnett_test_ExposedClass_Companion_exposedFun,
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass,
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedFun,
            Symbols.dev_rnett_test_ExposedClass_ExposedNestedClass_exposedProperty,
            Symbols.dev_rnett_test_ExposedClass_init,
            Symbols.dev_rnett_test_ExposedClass_init_param,
            Symbols.dev_rnett_test_ExposedClass_withTypeParameters_T,
            Symbols.dev_rnett_test_ExposedClass_withValueParameters_t,
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiver_receiver,
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverAll_this,
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverAll_receiver,
            Symbols.dev_rnett_test_ExposedClass_withContextParameters_a,
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverJustExtension_receiver,
            Symbols.dev_rnett_test_ExposedClass_withExtensionReceiverJustDispatch_this,
            Symbols.dev_rnett_test_ExposedClass_withDispatch_this,
            Symbols.dev_rnett_test_WithValueParams_T,
            Symbols.dev_rnett_test_TestEnum_A,
            Symbols.dev_rnett_test_TestAnnotation,
            Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation,
            Symbols.JvmMain.dev_rnett_test_jvmOnly
        )

        assertEquals(expectedSymbols, Symbols.ALL_SYMBOLS)
    }

    @Test
    fun testNonExportedSymbols() {
        // Test that non-exported symbols from TestDeclarations are not in Symbols.ALL_SYMBOLS

        // Check non-exported top-level function
        val notExposedFun = Symbol.TopLevelMember(
            packageName = NameSegments("dev", "rnett", "test"),
            name = "notExposedFun"
        )
        assertFalse(notExposedFun in Symbols.ALL_SYMBOLS)

        // Check non-exported top-level property
        val notExposedProperty = Symbol.TopLevelMember(
            packageName = NameSegments("dev", "rnett", "test"),
            name = "notExposedProperty"
        )
        assertFalse(notExposedProperty in Symbols.ALL_SYMBOLS)

        // Check non-exported nested class method
        val notExposedMethod = Symbol.NamedClassifierMember(
            classifier = Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            name = "notExposed"
        )
        assertFalse(notExposedMethod in Symbols.ALL_SYMBOLS)

        // Check non-exported nested class property
        val notExposedNestedProperty = Symbol.NamedClassifierMember(
            classifier = Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("ExposedClass", "ExposedNestedClass")
            ),
            name = "notExposedProperty"
        )
        assertFalse(notExposedNestedProperty in Symbols.ALL_SYMBOLS)
    }

    @Test
    fun testTestAnnotation() {
        assertTrue(Symbols.dev_rnett_test_TestAnnotation in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.Annotation::class.java,
            Symbols.dev_rnett_test_TestAnnotation::class.java.superclass
        )
        assertEquals(
            NameSegments("dev", "rnett", "test"),
            Symbols.dev_rnett_test_TestAnnotation.packageName
        )
        assertEquals(
            NameSegments("TestAnnotation"),
            Symbols.dev_rnett_test_TestAnnotation.classNames
        )

        // Verify annotation parameter names
        assertEquals("value", Symbols.dev_rnett_test_TestAnnotation.value.name)
        assertEquals("other", Symbols.dev_rnett_test_TestAnnotation.other.name)
        assertEquals("enu", Symbols.dev_rnett_test_TestAnnotation.enu.name)
        assertEquals("arr", Symbols.dev_rnett_test_TestAnnotation.arr.name)
        assertEquals("child", Symbols.dev_rnett_test_TestAnnotation.child.name)

        // Verify annotation parameter types
        assertEquals(AnnotationParameterType.String, Symbols.dev_rnett_test_TestAnnotation.value.type)
        assertEquals(AnnotationParameterType.Int, Symbols.dev_rnett_test_TestAnnotation.other.type)

        // For enum type, verify the enum class reference
        assertIs<AnnotationParameterType.Enum>(Symbols.dev_rnett_test_TestAnnotation.enu.type)
        val enumType = Symbols.dev_rnett_test_TestAnnotation.enu.type
        assertEquals(
            Symbol.Classifier(
                packageName = NameSegments("dev", "rnett", "test"),
                classNames = NameSegments("TestEnum")
            ),
            enumType.enumClass
        )

        // For array type, verify the element type
        assertIs<AnnotationParameterType.Array<*, *>>(Symbols.dev_rnett_test_TestAnnotation.arr.type)
        val arrayType = Symbols.dev_rnett_test_TestAnnotation.arr.type
        assertIs<AnnotationParameterType.String>(arrayType.elementType)

        // For annotation type, verify the annotation class reference
        assertIs<AnnotationParameterType.Annotation<*, *>>(Symbols.dev_rnett_test_TestAnnotation.child.type)
        val annotationType = Symbols.dev_rnett_test_TestAnnotation.child.type
        assertEquals(Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation, annotationType.annotationClass)
    }

    @Test
    fun testTestChildAnnotation() {
        assertTrue(Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation in Symbols.ALL_SYMBOLS)
        assertEquals(
            Symbol.Annotation::class.java,
            Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation::class.java.superclass
        )
        assertEquals(
            NameSegments("dev", "rnett", "test"),
            Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation.packageName
        )
        assertEquals(
            NameSegments("TestAnnotation", "TestChildAnnotation"),
            Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation.classNames
        )

        // Verify annotation parameter names
        assertEquals(Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation.test.name, "test")
        assertEquals(Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation.cls.name, "cls")

        // Verify annotation parameter types
        assertEquals(Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation.test.type, AnnotationParameterType.String)
        assertEquals(Symbols.dev_rnett_test_TestAnnotation_TestChildAnnotation.cls.type, AnnotationParameterType.KClass)
    }
}
