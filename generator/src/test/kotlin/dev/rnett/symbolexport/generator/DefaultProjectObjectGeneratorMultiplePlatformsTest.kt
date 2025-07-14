package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DefaultProjectObjectGeneratorMultiplePlatformsTest {

    @Test
    fun testGenerateMultiplePlatformsWithObjectNameAndCommonMain() {
        val objectName = "TestSymbols"
        val commonMainClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("CommonClass")
        )
        val commonMain = setOf(commonMainClassifier)

        val jvmClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JvmClass")
        )
        val jsClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JsClass")
        )

        val otherPlatforms = mapOf(
            NameSourceSet("jvmMain") to setOf(jvmClassifier),
            NameSourceSet("jsMain") to setOf(jsClassifier)
        )

        val javadocPrefix = "Test javadoc prefix"

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateMultiplePlatforms(commonMain, otherPlatforms, javadocPrefix)

        // Check that the object declaration is generated
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that the javadoc comment is generated
        assertTrue(result.contains("Test javadoc prefix"))
        assertTrue(result.contains("Generated from multiple source sets"))
        assertTrue(result.contains("the top level is from `commonMain`"))

        // Check that the common main property is generated
        assertTrue(result.contains("public val `test_package_CommonClass`: Classifier"))

        // Check that the platform-specific objects are generated
        assertTrue(result.contains("public object `JvmMain` {"))
        assertTrue(result.contains("public val `test_package_JvmClass`: Classifier"))
        assertTrue(result.contains("public object `JsMain` {"))
        assertTrue(result.contains("public val `test_package_JsClass`: Classifier"))

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("`TestSymbols`.`test_package_CommonClass`"))
        assertTrue(result.contains("JvmMain.`test_package_JvmClass`"))
        assertTrue(result.contains("JsMain.`test_package_JsClass`"))

        // Check that the object is closed
        assertTrue(result.contains("}"))
    }

    @Test
    fun testGenerateMultiplePlatformsWithoutObjectName() {
        val objectName = null
        val commonMain = null

        val jvmClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JvmClass")
        )
        val jsClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JsClass")
        )

        val otherPlatforms = mapOf(
            NameSourceSet("jvmMain") to setOf(jvmClassifier),
            NameSourceSet("jsMain") to setOf(jsClassifier)
        )

        val javadocPrefix = null

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateMultiplePlatforms(commonMain, otherPlatforms, javadocPrefix)

        // Check that the comment is generated
        assertTrue(result.contains("// Generated from multiple source sets"))

        // Check that the platform-specific properties are generated
        assertTrue(result.contains("public val `test_package_JvmClass`: Classifier"))
        assertTrue(result.contains("public val `test_package_JsClass`: Classifier"))

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("JvmMain.`test_package_JvmClass`"))
        assertTrue(result.contains("JsMain.`test_package_JsClass`"))

        // Check that there's no main object declaration, but platform objects are still generated
        assertTrue(!result.contains("public object `TestSymbols`"))
        assertTrue(result.contains("public object `JvmMain`"))
        assertTrue(result.contains("public object `JsMain`"))
    }

    @Test
    fun testGenerateMultiplePlatformsWithoutCommonMain() {
        val objectName = "TestSymbols"
        val commonMain = null

        val jvmClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JvmClass")
        )
        val jsClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JsClass")
        )

        val otherPlatforms = mapOf(
            NameSourceSet("jvmMain") to setOf(jvmClassifier),
            NameSourceSet("jsMain") to setOf(jsClassifier)
        )

        val javadocPrefix = null

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateMultiplePlatforms(commonMain, otherPlatforms, javadocPrefix)

        // Check that the object declaration is generated
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that the comment doesn't mention commonMain
        assertTrue(result.contains("Generated from multiple source sets"))
        assertTrue(!result.contains("the top level is from `commonMain`"))

        // Check that the platform-specific objects are generated
        assertTrue(result.contains("public object `JvmMain` {"))
        assertTrue(result.contains("public val `test_package_JvmClass`: Classifier"))
        assertTrue(result.contains("public object `JsMain` {"))
        assertTrue(result.contains("public val `test_package_JsClass`: Classifier"))

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("JvmMain.`test_package_JvmClass`"))
        assertTrue(result.contains("JsMain.`test_package_JsClass`"))
    }

    @Test
    fun testGenerateMultiplePlatformsWithEmptyOtherPlatforms() {
        val objectName = "TestSymbols"
        val commonMainClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("CommonClass")
        )
        val commonMain = setOf(commonMainClassifier)
        val otherPlatforms = emptyMap<NameSourceSet, Set<InternalName>>()
        val javadocPrefix = null

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateMultiplePlatforms(commonMain, otherPlatforms, javadocPrefix)

        // Check that the object declaration is generated
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that the common main property is generated
        assertTrue(result.contains("public val `test_package_CommonClass`: Classifier"))

        // Check that no platform-specific objects are generated
        assertTrue(!result.contains("public object `JvmMain`"))
        assertTrue(!result.contains("public object `JsMain`"))

        // Check that the ALL_SYMBOLS property is generated with only common main symbols
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("`TestSymbols`.`test_package_CommonClass`"))
    }
}
