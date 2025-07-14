package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DefaultProjectObjectGeneratorSingleStringTest {

    @Test
    fun testGenerateSingleStringWithObjectName() {
        val objectName = "TestSymbols"
        val sourceSet = NameSourceSet("commonMain")
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val symbols = setOf(classifier)
        val javadocPrefix = "Test javadoc prefix"

        val result = DefaultProjectObjectGenerator.Companion.generateSingleString(
            objectName,
            sourceSet,
            symbols,
            javadocPrefix
        )

        // Check that the object declaration is generated
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that the javadoc comment is generated
        assertTrue(result.contains("Test javadoc prefix"))
        assertTrue(result.contains("Generated from the source set `commonMain`"))

        // Check that the property is generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))

        // Check that the object is closed
        assertTrue(result.contains("}"))
    }

    @Test
    fun testGenerateSingleStringWithoutObjectName() {
        val objectName = null
        val sourceSet = NameSourceSet("commonMain")
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val symbols = setOf(classifier)
        val javadocPrefix = null

        val result = DefaultProjectObjectGenerator.Companion.generateSingleString(
            objectName,
            sourceSet,
            symbols,
            javadocPrefix
        )

        // Check that the comment is generated
        assertTrue(result.contains("//  Generated from the source set `commonMain`"))

        // Check that the property is generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))

        // Check that the end comment is generated
        assertTrue(result.contains("// End` commonMain`"))

        // Check that there's no object declaration
        assertTrue(!result.contains("public object"))
        assertTrue(!result.contains("}"))
    }

    @Test
    fun testGenerateSingleStringWithAdditional() {
        val objectName = "TestSymbols"
        val sourceSet = NameSourceSet("commonMain")
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val symbols = setOf(classifier)
        val javadocPrefix = null
        val additional: StringBuilder.() -> Unit = { appendLine("// Additional content") }

        val result = DefaultProjectObjectGenerator.Companion.generateSingleString(
            objectName,
            sourceSet,
            symbols,
            javadocPrefix,
            additional
        )

        // Check that the additional content is included
        assertTrue(result.contains("// Additional content"))
    }

    @Test
    fun testGenerateSingleStringWithMultipleSymbols() {
        val objectName = "TestSymbols"
        val sourceSet = NameSourceSet("commonMain")
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )
        val symbols = setOf(classifier, topLevelMember)
        val javadocPrefix = null

        val result = DefaultProjectObjectGenerator.Companion.generateSingleString(
            objectName,
            sourceSet,
            symbols,
            javadocPrefix
        )

        // Check that both properties are generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))
        assertTrue(result.contains("public val `test_package_testFunction`: TopLevelMember"))
    }

    @Test
    fun testGenerateSingleStringWithEmptySymbols() {
        val objectName = "TestSymbols"
        val sourceSet = NameSourceSet("commonMain")
        val symbols = emptySet<InternalName>()
        val javadocPrefix = null

        val result = DefaultProjectObjectGenerator.Companion.generateSingleString(
            objectName,
            sourceSet,
            symbols,
            javadocPrefix
        )

        // Check that the object declaration is generated
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that no properties are generated
        assertTrue(!result.contains("public val"))
    }
}
