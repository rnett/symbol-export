package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultProjectObjectGeneratorConstructorTest {
    // Empty set of referencable symbols for most tests
    private val referencable = emptySet<InternalName>()

    // Tests for referencing existing fields in constructors
    @Test
    fun testConstructorWithReferencableClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        // Create a set with the classifier as a referencable symbol
        val referencableSet = setOf(classifier)

        val classifierMember = InternalName.ClassifierMember(
            classifier = classifier,
            name = "testMethod"
        )

        val result = InternalNameGenerationHandler.generateConstructor(classifierMember, referencableSet)

        // The classifier should be referenced by field name instead of being constructed
        assertEquals(
            "NamedClassifierMember(classifier = test_package_TestClass, name = \"testMethod\")",
            result
        )
    }

    @Test
    fun testConstructorWithReferencableOwner() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val enumEntry = InternalName.EnumEntry(
            owner = classifier,
            name = "ENTRY_ONE",
            ordinal = 0
        )

        // Create a set with the classifier as a referencable symbol
        val referencableSet = setOf(classifier)

        val result = InternalNameGenerationHandler.generateConstructor(enumEntry, referencableSet)

        // The classifier should be referenced by field name instead of being constructed
        assertEquals(
            "EnumEntry(enumClass = test_package_TestClass, entryName = \"ENTRY_ONE\", entryOrdinal = 0)",
            result
        )
    }

    @Test
    fun testConstructorWithMultipleReferencableSymbols() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

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

        // Create a set with both symbols as referencable
        val referencableSet = setOf(classifier, topLevelMember)

        // Test with a constructor that references topLevelMember
        val result = InternalNameGenerationHandler.generateConstructor(valueParameter, referencableSet)

        // The topLevelMember should be referenced by field name instead of being constructed
        assertEquals(
            "ValueParameter(owner=test_package_testFunction, index=0, indexInValueParameters=0, name=\"param1\")",
            result
        )
    }

    @Test
    fun testConstructorClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = InternalNameGenerationHandler.generateConstructor(classifier, referencable)

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

        val result = InternalNameGenerationHandler.generateConstructor(classifierMember, referencable)

        assertEquals(
            "NamedClassifierMember(classifier = Classifier(packageName = NameSegments(\"test\", \"package\"), classNames = NameSegments(\"TestClass\")), name = \"testMethod\")",
            result
        )
    }

    @Test
    fun testConstructorTopLevelMember() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )

        val result = InternalNameGenerationHandler.generateConstructor(topLevelMember, referencable)

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

        val result = InternalNameGenerationHandler.generateConstructor(enumEntry, referencable)

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

        val result = InternalNameGenerationHandler.generateConstructor(constructor, referencable)

        assertEquals(
            "Constructor(classifier = Classifier(packageName = NameSegments(\"test\", \"package\"), classNames = NameSegments(\"TestClass\")))",
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

        val result = InternalNameGenerationHandler.generateConstructor(typeParameter, referencable)

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

        val result = InternalNameGenerationHandler.generateConstructor(valueParameter, referencable)

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

        val result = InternalNameGenerationHandler.generateConstructor(contextParameter, referencable)

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

        val result = InternalNameGenerationHandler.generateConstructor(extensionReceiver, referencable)

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

        val result = InternalNameGenerationHandler.generateConstructor(dispatchReceiver, referencable)

        assertEquals(
            "DispatchReceiverParameter(owner=TopLevelMember(packageName = NameSegments(\"test\", \"package\"), name = \"testFunction\"), index=0, name=\"this\")",
            result
        )
    }
}