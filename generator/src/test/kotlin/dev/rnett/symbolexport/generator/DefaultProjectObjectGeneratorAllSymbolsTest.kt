package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProjectObjectGeneratorAllSymbolsTest {

    @Test
    fun testGenerateAllSymbolsWithObjectName() {
        val objectName = "TestSymbols"
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val topLevelMember = InternalName.TopLevelMember(
            packageName = listOf("test", "package"),
            name = "testFunction"
        )
        val topLevelSymbols = setOf(classifier, topLevelMember)
        val otherSymbols = emptyMap<String, Set<InternalName>>()

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateAllSymbolsProperty(objectName, topLevelSymbols, otherSymbols)

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))

        // Check that the symbols are included
        assertTrue(result.contains("`TestSymbols`.`test_package_TestClass`"))
        assertTrue(result.contains("`TestSymbols`.`test_package_testFunction`"))
    }

    @Test
    fun testGenerateAllSymbolsWithoutObjectName() {
        val objectName = null
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val topLevelSymbols = setOf(classifier)
        val otherSymbols = emptyMap<String, Set<InternalName>>()

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateAllSymbolsProperty(objectName, topLevelSymbols, otherSymbols)

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))

        // Check that the symbols are included with the default object name
        assertTrue(result.contains("`Symbols`.`test_package_TestClass`"))
    }

    @Test
    fun testGenerateAllSymbolsWithOtherSymbols() {
        val objectName = "TestSymbols"
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )
        val topLevelSymbols = setOf(classifier)

        val jvmClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JvmClass")
        )
        val jsClassifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("JsClass")
        )

        val otherSymbols = mapOf(
            "Jvm" to setOf(jvmClassifier),
            "Js" to setOf(jsClassifier)
        )

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateAllSymbolsProperty(objectName, topLevelSymbols, otherSymbols)

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))

        // Check that the top-level symbols are included
        assertTrue(result.contains("`TestSymbols`.`test_package_TestClass`"))

        // Check that the other symbols are included with their prefixes
        assertTrue(result.contains("Jvm.`test_package_JvmClass`"))
        assertTrue(result.contains("Js.`test_package_JsClass`"))
    }

    @Test
    fun testGenerateAllSymbolsWithEmptySymbols() {
        val objectName = "TestSymbols"
        val topLevelSymbols = emptySet<InternalName>()
        val otherSymbols = emptyMap<String, Set<InternalName>>()

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateAllSymbolsProperty(objectName, topLevelSymbols, otherSymbols)

        // Check that the ALL_SYMBOLS property is generated with an empty set
        assertEquals(
            """
            val ALL_SYMBOLS: Set<Symbol> = setOf(
            )
            """.trimIndent(),
            result.trimIndent()
        )
    }

    @Test
    fun testGenerateAllSymbolsWithAnnotation() {
        val objectName = "TestSymbols"
        val classifier = InternalName.Classifier(
            packageName = listOf("test", "package"),
            classNames = listOf("TestClass")
        )

        // Create an annotation
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = listOf(
                InternalName.Annotation.Parameter(
                    "stringParam",
                    0,
                    AnnotationParameterType.Primitive.STRING
                ),
                InternalName.Annotation.Parameter(
                    "intParam",
                    1,
                    AnnotationParameterType.Primitive.INT
                )
            )
        )

        val topLevelSymbols = setOf(classifier, annotation)
        val otherSymbols = emptyMap<String, Set<InternalName>>()

        val generator = DefaultProjectObjectGenerator(objectName, emptySet())
        val result = generator.generateAllSymbolsProperty(objectName, topLevelSymbols, otherSymbols)

        // Check that the ALL_SYMBOLS property is generated
        assertTrue(result.contains("val ALL_SYMBOLS: Set<Symbol> = setOf("))

        // Check that both the classifier and annotation symbols are included
        assertTrue(result.contains("`TestSymbols`.`test_package_TestClass`"))
        assertTrue(result.contains("`TestSymbols`.`test_package_TestAnnotation`"))
    }
}