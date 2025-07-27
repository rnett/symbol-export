package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DefaultProjectObjectGeneratorPropertyGenerationTest {
    // Empty set of referencable symbols for most tests
    private val referencable = emptySet<InternalName>()

    @Test
    fun testGeneratePropertyClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = CodeFormatter.generateSymbol(classifier, referencable)

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

        val result = CodeFormatter.generateSymbol(classifierMember, referencable)

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

        val result = CodeFormatter.generateSymbol(topLevelMember, referencable)

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

        val result = CodeFormatter.generateSymbol(enumEntry, referencable)

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

        val result = CodeFormatter.generateSymbol(constructor, referencable)

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

        val result = CodeFormatter.generateSymbol(typeParameter, referencable)

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

        val result = CodeFormatter.generateSymbol(valueParameter, referencable)

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

        val result = CodeFormatter.generateSymbol(contextParameter, referencable)

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

        val result = CodeFormatter.generateSymbol(extensionReceiver, referencable)

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

        val result = CodeFormatter.generateSymbol(dispatchReceiver, referencable)

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