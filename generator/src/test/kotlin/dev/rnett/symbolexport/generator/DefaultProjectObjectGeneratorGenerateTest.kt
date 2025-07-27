package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultProjectObjectGeneratorGenerateTest {

    @Test
    fun testGenerateWithEmptySymbols() {
        val objectName = "TestSymbols"
        val names = emptySet<NameFromSourceSet>()

        val generator = DefaultProjectObjectGenerator(objectName, names)
        val result = generator.generate("Test javadoc prefix")

        // Should return null for empty symbols
        assertNull(result)
    }

    @Test
    fun testGenerateWithSingleSourceSet() {
        val objectName = "TestSymbols"
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val names = setOf(
            NameFromSourceSet("commonMain", classifier)
        )

        val generator = DefaultProjectObjectGenerator(objectName, names)
        val result = generator.generate("Test javadoc prefix")

        // Should call generateSingle
        assertTrue(result != null)
        assertTrue(result.contains("public object `TestSymbols` {"))
        assertTrue(result.contains("Test javadoc prefix"))
        assertTrue(result.contains("Generated from the source set `commonMain`"))
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
    }

    @Test
    fun testGenerateWithMultipleSourceSets() {
        val objectName = "TestSymbols"
        val commonClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("CommonClass")
        )
        val jvmClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JvmClass")
        )
        val jsClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JsClass")
        )

        val names = setOf(
            NameFromSourceSet("commonMain", commonClassifier),
            NameFromSourceSet("jvmMain", jvmClassifier),
            NameFromSourceSet("jsMain", jsClassifier)
        )

        val generator = DefaultProjectObjectGenerator(objectName, names)
        val result = generator.generate("Test javadoc prefix")

        // Should call generateMultiplePlatforms
        assertTrue(result != null)
        assertTrue(result.contains("public object `TestSymbols` {"))
        assertTrue(result.contains("Test javadoc prefix"))
        assertTrue(result.contains("Generated from multiple source sets"))
        assertTrue(result.contains("the top level is from `commonMain`"))

        // Check that common main property is generated
        assertTrue(result.contains("public val `test_package_CommonClass`: Classifier"))

        // Check that platform-specific objects are generated
        assertTrue(result.contains("public object `JvmMain` {"))
        assertTrue(result.contains("public val `test_package_JvmClass`: Classifier"))
        assertTrue(result.contains("public object `JsMain` {"))
        assertTrue(result.contains("public val `test_package_JsClass`: Classifier"))

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("`TestSymbols`.`test_package_CommonClass`"))
        assertTrue(result.contains("JvmMain.`test_package_JvmClass`"))
        assertTrue(result.contains("JsMain.`test_package_JsClass`"))
    }

    @Test
    fun testGenerateWithMultipleSourceSetsWithoutCommonMain() {
        val objectName = "TestSymbols"
        val jvmClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JvmClass")
        )
        val jsClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JsClass")
        )

        val names = setOf(
            NameFromSourceSet("jvmMain", jvmClassifier),
            NameFromSourceSet("jsMain", jsClassifier)
        )

        val generator = DefaultProjectObjectGenerator(objectName, names)
        val result = generator.generate("Test javadoc prefix")

        // Should call generateMultiplePlatforms without commonMain
        assertTrue(result != null)
        assertTrue(result.contains("public object `TestSymbols` {"))
        assertTrue(result.contains("Test javadoc prefix"))
        assertTrue(result.contains("Generated from multiple source sets"))
        assertTrue(!result.contains("the top level is from `commonMain`"))

        // Check that platform-specific objects are generated
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
    fun testGenerateWithMultipleSourceSetsFilteringCommonSymbols() {
        val objectName = "TestSymbols"
        val commonClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("CommonClass")
        )
        val jvmClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JvmClass")
        )

        // Both commonMain and jvmMain have the same symbol - it should be filtered from jvmMain
        val names = setOf(
            NameFromSourceSet("commonMain", commonClassifier),
            NameFromSourceSet("jvmMain", commonClassifier),
            NameFromSourceSet("jvmMain", jvmClassifier)
        )

        val generator = DefaultProjectObjectGenerator(objectName, names)
        val result = generator.generate(null)

        // Should call generateMultiplePlatforms
        assertTrue(result != null)
        assertTrue(result.contains("Generated from multiple source sets"))

        // Check that common main property is generated
        assertTrue(result.contains("public val `test_package_CommonClass`: Classifier"))

        // Check that only the JVM-specific class is in the JVM object (common class should be filtered out)
        assertTrue(result.contains("public object `JvmMain` {"))
        assertTrue(result.contains("public val `test_package_JvmClass`: Classifier"))

        // The common class should not appear twice in the JVM section
        val jvmSectionStart = result.indexOf("public object `JvmMain` {")
        val jvmSectionEnd = result.indexOf("}", jvmSectionStart)
        val jvmSection = result.substring(jvmSectionStart, jvmSectionEnd)
        assertTrue(!jvmSection.contains("public val `test_package_CommonClass`: Classifier"))
    }

    @Test
    fun testGenerateWithAnnotation() {
        val objectName = "TestSymbols"

        // Create an annotation
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "stringParam" to AnnotationParameterType.Primitive.STRING,
                "intParam" to AnnotationParameterType.Primitive.INT
            )
        )

        val names = setOf(
            NameFromSourceSet("commonMain", annotation)
        )

        val generator = DefaultProjectObjectGenerator(objectName, names)
        val result = generator.generate("Test javadoc prefix")

        // Should call generateSingle
        assertTrue(result != null)
        assertTrue(result.contains("public object `TestSymbols` {"))
        assertTrue(result.contains("Test javadoc prefix"))
        assertTrue(result.contains("Generated from the source set `commonMain`"))

        // Check that annotation is generated
        assertTrue(result.contains("public val `test_package_TestAnnotation`: test_package_TestAnnotation_Spec"))

        // Check that ALL_SYMBOLS includes the annotation
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("`TestSymbols`.`test_package_TestAnnotation`"))
    }

    @Test
    fun testGenerateWithMixedSymbolsAndAnnotations() {
        val objectName = "TestSymbols"
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        // Create an annotation
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "stringParam" to AnnotationParameterType.Primitive.STRING,
                "intParam" to AnnotationParameterType.Primitive.INT
            )
        )

        val names = setOf(
            NameFromSourceSet("commonMain", classifier),
            NameFromSourceSet("commonMain", annotation)
        )

        val generator = DefaultProjectObjectGenerator(objectName, names)
        val result = generator.generate("Test javadoc prefix")

        // Should call generateSingle
        assertTrue(result != null)
        assertTrue(result.contains("public object `TestSymbols` {"))

        // Check that both classifier and annotation are generated
        assertTrue(result.contains("public val `test_package_TestClass`: Classifier"))
        assertTrue(result.contains("public val `test_package_TestAnnotation`: test_package_TestAnnotation_Spec"))

        // Check that ALL_SYMBOLS includes both symbols
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))
        assertTrue(result.contains("`TestSymbols`.`test_package_TestClass`"))
        assertTrue(result.contains("`TestSymbols`.`test_package_TestAnnotation`"))
    }
}