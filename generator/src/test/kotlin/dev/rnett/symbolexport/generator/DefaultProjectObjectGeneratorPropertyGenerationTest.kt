package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Test wrapper for utility methods
 * This class provides access to the utility methods for testing purposes
 */
class DefaultProjectObjectGeneratorTestWrapper {
    companion object {
        // Expose the methods from utility classes
        fun generateProperty(name: InternalName): String =
            CodeFormatter.generateProperty(name)

        fun nameSegmentsOf(segments: List<String>): String =
            InternalNameHandler.nameSegmentsOf(segments)

        fun InternalName.constructor(): String =
            InternalNameHandler.generateConstructor(this)

        fun InternalName.allParts(): List<String> =
            InternalNameHandler.getAllParts(this)

        fun InternalName.type(): String =
            InternalNameHandler.getType(this)

        fun InternalName.fieldName(): String =
            InternalNameHandler.getFieldName(this)
    }
}

class DefaultProjectObjectGeneratorPropertyGenerationTest {
    // Use the test wrapper to access the methods
    private val testWrapper = DefaultProjectObjectGeneratorTestWrapper.Companion

    @Test
    fun testGeneratePropertyClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = testWrapper.generateProperty(classifier)

        assertEquals(
            """
            /**
             * Generated from `test.package.TestClass`
             */
            public val `test_package_TestClass`: Classifier = Classifier(packageName = NameSegments("test", "package"), classNames = NameSegments("TestClass"))
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyClassifierMember() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val classifierMember = InternalName.ClassifierMember(
            classifier = classifier,
            name = "testMethod"
        )

        val result = testWrapper.generateProperty(classifierMember)

        assertEquals(
            """
            /**
             * Generated from `test.package.TestClass.testMethod`
             */
            public val `test_package_TestClass_testMethod`: ClassifierMember = ClassifierMember(classifier = Classifier(packageName = NameSegments("test", "package"), classNames = NameSegments("TestClass")), name = "testMethod")
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyTopLevelMember() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )

        val result = testWrapper.generateProperty(topLevelMember)

        assertEquals(
            """
            /**
             * Generated from `test.package.testFunction`
             */
            public val `test_package_testFunction`: TopLevelMember = TopLevelMember(packageName = NameSegments("test", "package"), name = "testFunction")
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyEnumEntry() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestEnum")
        )
        val enumEntry = InternalName.EnumEntry(
            owner = classifier,
            name = "ENTRY_ONE",
            ordinal = 0
        )

        val result = testWrapper.generateProperty(enumEntry)

        assertEquals(
            """
            /**
             * Generated from `test.package.TestEnum.ENTRY_ONE`
             */
            public val `test_package_TestEnum_ENTRY_ONE`: EnumEntry = EnumEntry(enumClass = Classifier(packageName = NameSegments("test", "package"), classNames = NameSegments("TestEnum")), entryName = "ENTRY_ONE", entryOrdinal = 0)
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyConstructor() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val constructor = InternalName.Constructor(
            classifier = classifier,
            name = "<init>"
        )

        val result = testWrapper.generateProperty(constructor)

        assertEquals(
            """
            /**
             * Generated from `test.package.TestClass.<init>`
             */
            public val `test_package_TestClass_init`: Constructor = Constructor(classifier = Classifier(packageName = NameSegments("test", "package"), classNames = NameSegments("TestClass")), name = "<init>")
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyTypeParameter() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val typeParameter = InternalName.TypeParameter(
            owner = classifier,
            name = "T",
            index = 0
        )

        val result = testWrapper.generateProperty(typeParameter)

        assertEquals(
            """
            /**
             * Generated from `test.package.TestClass.T`
             */
            public val `test_package_TestClass_T`: TypeParameter = TypeParameter(owner=Classifier(packageName = NameSegments("test", "package"), classNames = NameSegments("TestClass")), index=0, name="T")
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyValueParameter() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )
        val valueParameter = InternalName.IndexedParameter(
            owner = topLevelMember,
            name = "param1",
            index = 0,
            indexInList = 0,
            type = VALUE
        )

        val result = testWrapper.generateProperty(valueParameter)

        assertEquals(
            """
            /**
             * Generated from `test.package.testFunction.param1`
             */
            public val `test_package_testFunction_param1`: ValueParameter = ValueParameter(owner=TopLevelMember(packageName = NameSegments("test", "package"), name = "testFunction"), index=0, indexInValueParameters=0, name="param1")
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyContextParameter() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )
        val contextParameter = InternalName.IndexedParameter(
            owner = topLevelMember,
            name = "context",
            index = 0,
            indexInList = 0,
            type = CONTEXT
        )

        val result = testWrapper.generateProperty(contextParameter)

        assertEquals(
            """
            /**
             * Generated from `test.package.testFunction.context`
             */
            public val `test_package_testFunction_context`: ContextParameter = ContextParameter(owner=TopLevelMember(packageName = NameSegments("test", "package"), name = "testFunction"), index=0, indexInContextParameters=0, name="context")
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyExtensionReceiverParameter() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )
        val extensionReceiver = InternalName.ReceiverParameter(
            owner = topLevelMember,
            name = "this",
            index = 0,
            type = EXTENSION
        )

        val result = testWrapper.generateProperty(extensionReceiver)

        assertEquals(
            """
            /**
             * Generated from `test.package.testFunction.this`
             */
            public val `test_package_testFunction_this`: ExtensionReceiverParameter = ExtensionReceiverParameter(owner=TopLevelMember(packageName = NameSegments("test", "package"), name = "testFunction"), index=0, name="this")
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGeneratePropertyDispatchReceiverParameter() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )
        val dispatchReceiver = InternalName.ReceiverParameter(
            owner = topLevelMember,
            name = "this",
            index = 0,
            type = DISPATCH
        )

        val result = testWrapper.generateProperty(dispatchReceiver)

        assertEquals(
            """
            /**
             * Generated from `test.package.testFunction.this`
             */
            public val `test_package_testFunction_this`: DispatchReceiverParameter = DispatchReceiverParameter(owner=TopLevelMember(packageName = NameSegments("test", "package"), name = "testFunction"), index=0, name="this")
            """.trimIndent(),
            result.trimIndent()
        )
    }
}
