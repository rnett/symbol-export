package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DefaultProjectObjectGeneratorHelperMethodsTest {
    @Test
    fun testNameSegmentsOf() {
        val segments = listOf("test", "package", "Class")
        val result = InternalNameGenerationHandler.nameSegmentsOf(segments)

        assertEquals("NameSegments(\"test\", \"package\", \"Class\")", result)
    }

    @Test
    fun testAllPartsClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = InternalNameGenerationHandler.getAllParts(classifier)

        assertEquals(listOf("test", "package", "TestClass"), result)
    }

    @Test
    fun testAllPartsClassifierMember() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val classifierMember = InternalName.ClassifierMember(
            classifier = classifier,
            name = "testMethod"
        )

        val result = InternalNameGenerationHandler.getAllParts(classifierMember)

        assertEquals(listOf("test", "package", "TestClass", "testMethod"), result)
    }

    @Test
    fun testAllPartsTopLevelMember() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )

        val result = InternalNameGenerationHandler.getAllParts(topLevelMember)

        assertEquals(listOf("test", "package", "testFunction"), result)
    }

    @Test
    fun testTypeClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = InternalNameGenerationHandler.getType(classifier)

        assertEquals("Classifier", result)
    }

    @Test
    fun testTypeClassifierMember() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val classifierMember = InternalName.ClassifierMember(
            classifier = classifier,
            name = "testMethod"
        )

        val result = InternalNameGenerationHandler.getType(classifierMember)

        assertEquals("NamedClassifierMember", result)
    }

    @Test
    fun testFieldName() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = InternalNameGenerationHandler.getFieldName(classifier)

        assertEquals("test_package_TestClass", result)
    }

    @Test
    fun testFieldNameWithSpecialCharacters() {
        val constructor = InternalName.Constructor(
            classifier = InternalName.Classifier(
                packageName = listOf("test", "package"),
                classNames = listOf("TestClass")
            ),
            name = "<init>"
        )

        val result = InternalNameGenerationHandler.getFieldName(constructor)

        assertEquals("test_package_TestClass_init", result)
    }
}