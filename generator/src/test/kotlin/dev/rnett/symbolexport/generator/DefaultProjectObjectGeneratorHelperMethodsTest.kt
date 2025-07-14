package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultProjectObjectGeneratorHelperMethodsTest {
    // Use the test wrapper to access the methods
    private val testWrapper = DefaultProjectObjectGeneratorTestWrapper.Companion

    @Test
    fun testNameSegmentsOf() {
        val segments = listOf("test", "package", "Class")
        val result = testWrapper.nameSegmentsOf(segments)

        assertEquals("NameSegments(\"test\", \"package\", \"Class\")", result)
    }

    @Test
    fun testAllPartsClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = testWrapper.run { classifier.allParts() }

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

        val result = testWrapper.run { classifierMember.allParts() }

        assertEquals(listOf("test", "package", "TestClass", "testMethod"), result)
    }

    @Test
    fun testAllPartsTopLevelMember() {
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )

        val result = testWrapper.run { topLevelMember.allParts() }

        assertEquals(listOf("test", "package", "testFunction"), result)
    }

    @Test
    fun testTypeClassifier() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = testWrapper.run { classifier.type() }

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

        val result = testWrapper.run { classifierMember.type() }

        assertEquals("ClassifierMember", result)
    }

    @Test
    fun testFieldName() {
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        val result = testWrapper.run { classifier.fieldName() }

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

        val result = testWrapper.run { constructor.fieldName() }

        assertEquals("test_package_TestClass_init", result)
    }
}