package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultProjectObjectGeneratorConstructorTest {
    // Use the test wrapper to access the methods
    private val testWrapper = DefaultProjectObjectGeneratorTestWrapper.Companion

    @Test
    fun testConstructorClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = testWrapper.run { classifier.constructor() }

        assertEquals(
            "Classifier(packageName = NameSegments(\"test\", \"package\"), classNames = NameSegments(\"TestClass\"))",
            result
        )
    }

    @Test
    fun testConstructorClassifierMember() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val classifierMember = InternalName.ClassifierMember(
            classifier = classifier,
            name = "testMethod"
        )

        val result = testWrapper.run { classifierMember.constructor() }

        assertEquals(
            "ClassifierMember(classifier = Classifier(packageName = NameSegments(\"test\", \"package\"), classNames = NameSegments(\"TestClass\")), name = \"testMethod\")",
            result
        )
    }

    @Test
    fun testConstructorTopLevelMember() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )

        val result = testWrapper.run { topLevelMember.constructor() }

        assertEquals(
            "TopLevelMember(packageName = NameSegments(\"test\", \"package\"), name = \"testFunction\")",
            result
        )
    }

    @Test
    fun testConstructorEnumEntry() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestEnum")
        )
        val enumEntry = InternalName.EnumEntry(
            owner = classifier,
            name = "ENTRY_ONE",
            ordinal = 0
        )

        val result = testWrapper.run { enumEntry.constructor() }

        assertEquals(
            "EnumEntry(enumClass = Classifier(packageName = NameSegments(\"test\", \"package\"), classNames = NameSegments(\"TestEnum\")), entryName = \"ENTRY_ONE\", entryOrdinal = 0)",
            result
        )
    }

    @Test
    fun testConstructorConstructor() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val constructor = InternalName.Constructor(
            classifier = classifier,
            name = "<init>"
        )

        val result = testWrapper.run { constructor.constructor() }

        assertEquals(
            "Constructor(classifier = Classifier(packageName = NameSegments(\"test\", \"package\"), classNames = NameSegments(\"TestClass\")), name = \"<init>\")",
            result
        )
    }

    @Test
    fun testConstructorTypeParameter() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val typeParameter = InternalName.TypeParameter(
            owner = classifier,
            name = "T",
            index = 0
        )

        val result = testWrapper.run { typeParameter.constructor() }

        assertEquals(
            "TypeParameter(owner=Classifier(packageName = NameSegments(\"test\", \"package\"), classNames = NameSegments(\"TestClass\")), index=0, name=\"T\")",
            result
        )
    }

    @Test
    fun testConstructorValueParameter() {
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

        val result = testWrapper.run { valueParameter.constructor() }

        assertEquals(
            "ValueParameter(owner=TopLevelMember(packageName = NameSegments(\"test\", \"package\"), name = \"testFunction\"), index=0, indexInValueParameters=0, name=\"param1\")",
            result
        )
    }

    @Test
    fun testConstructorContextParameter() {
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

        val result = testWrapper.run { contextParameter.constructor() }

        assertEquals(
            "ContextParameter(owner=TopLevelMember(packageName = NameSegments(\"test\", \"package\"), name = \"testFunction\"), index=0, indexInContextParameters=0, name=\"context\")",
            result
        )
    }

    @Test
    fun testConstructorExtensionReceiverParameter() {
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

        val result = testWrapper.run { extensionReceiver.constructor() }

        assertEquals(
            "ExtensionReceiverParameter(owner=TopLevelMember(packageName = NameSegments(\"test\", \"package\"), name = \"testFunction\"), index=0, name=\"this\")",
            result
        )
    }

    @Test
    fun testConstructorDispatchReceiverParameter() {
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

        val result = testWrapper.run { dispatchReceiver.constructor() }

        assertEquals(
            "DispatchReceiverParameter(owner=TopLevelMember(packageName = NameSegments(\"test\", \"package\"), name = \"testFunction\"), index=0, name=\"this\")",
            result
        )
    }
}