package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DefaultProjectObjectGeneratorSingleTest {

    @Test
    fun testGenerateSingleWithObjectName() {
        val objectName = "TestSymbols"
        val sourceSet = NameSourceSet("commonMain")
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val symbols = setOf(classifier)
        val javadocPrefix = "Test javadoc prefix"

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateSingle(sourceSet, symbols, javadocPrefix)

        // Check that the object declaration is generated
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that the javadoc comment is generated
        assertTrue(result.contains("Test javadoc prefix"))
        assertTrue(result.contains("Generated from the source set `commonMain`"))

        // Check that the property is generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("`TestSymbols`.`test_package_TestClass`"))

        // Check that the object is closed
        assertTrue(result.contains("}"))
    }

    @Test
    fun testGenerateSingleWithoutObjectName() {
        val objectName = null
        val sourceSet = NameSourceSet("commonMain")
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val symbols = setOf(classifier)
        val javadocPrefix = null

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateSingle(sourceSet, symbols, javadocPrefix)

        // Check that the comment is generated
        assertTrue(result.contains("//  Generated from the source set `commonMain`"))

        // Check that the property is generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("`Symbols`.`test_package_TestClass`"))

        // Check that the end comment is generated
        assertTrue(result.contains("// End` commonMain`"))
    }

    @Test
    fun testGenerateSingleWithMultipleSymbols() {
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

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateSingle(sourceSet, symbols, javadocPrefix)

        // Check that both properties are generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))
        assertTrue(result.contains("public val `test_package_testFunction`: TopLevelMember"))

        // Check that both symbols are in the ALL_SYMBOLS property
        assertTrue(result.contains("`TestSymbols`.`test_package_TestClass`"))
        assertTrue(result.contains("`TestSymbols`.`test_package_testFunction`"))
    }

    @Test
    fun testGenerateSingleWithEmptySymbols() {
        val objectName = "TestSymbols"
        val sourceSet = NameSourceSet("commonMain")
        val symbols = emptySet<InternalName>()
        val javadocPrefix = null

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateSingle(sourceSet, symbols, javadocPrefix)

        // Check that the object declaration is generated
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that no properties are generated
        assertTrue(!result.contains("public val `test_package_"))

        // Check that the ALL_SYMBOLS property is generated with an empty set
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(!result.contains("`TestSymbols`.`test_package_"))
    }
}