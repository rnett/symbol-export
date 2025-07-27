package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.AnnotationParameterType
import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalNameEntry
import dev.rnett.symbolexport.internal.ProjectCoordinates
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SymbolGeneratorTest {

    // Mock ProjectObjectGenerator for testing
    private class MockProjectObjectGenerator : ProjectObjectGenerator {
        var lastObjectName: String? = null
        var lastNames: Set<NameFromSourceSet>? = null
        var lastJavadocPrefix: String? = null
        var returnValue: String? = "mock-generated-content"

        override fun generate(objectName: String?, names: Set<NameFromSourceSet>, javadocPrefix: String?): String? {
            lastObjectName = objectName
            lastNames = names
            lastJavadocPrefix = javadocPrefix
            return returnValue
        }

        fun reset() {
            lastObjectName = null
            lastNames = null
            lastJavadocPrefix = null
            returnValue = "mock-generated-content"
        }
    }

    private fun createTestEntry(
        projectName: String = "test-project",
        group: String = "com.example",
        artifact: String = "test-artifact",
        version: String = "1.0.0",
        sourceSetName: String = "commonMain",
        name: InternalName = InternalName.Classifier(listOf("test"), listOf("TestClass"))
    ) = InternalNameEntry(
        projectName = projectName,
        projectCoordinates = ProjectCoordinates(group, artifact, version),
        sourceSetName = sourceSetName,
        name = name
    )

    @Test
    fun testGenerateSymbolsFileWithEmptyEntries() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val result = symbolGenerator.generateSymbolsFile(emptyList())

        // Empty entries should produce an empty symbols file, not null
        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("}"))
    }

    @Test
    fun testGenerateSymbolsFileWithFlattenProjectsTrue() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", true, mockGenerator)

        val entries = listOf(
            createTestEntry(
                projectName = "project-a",
                name = InternalName.Classifier(listOf("com", "example"), listOf("ClassA"))
            ),
            createTestEntry(
                projectName = "project-b",
                name = InternalName.Classifier(listOf("com", "example"), listOf("ClassB"))
            )
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("Symbols from project `project-a`"))
        assertTrue(result.contains("Symbols from project `project-b`"))
        assertTrue(result.contains("mock-generated-content"))
    }

    @Test
    fun testGenerateSymbolsFileWithFlattenProjectsFalse() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val entries = listOf(
            createTestEntry(
                projectName = "project-a",
                name = InternalName.Classifier(listOf("com", "example"), listOf("ClassA"))
            )
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("Symbols from project `project-a`"))
        assertTrue(result.contains("mock-generated-content"))
    }

    @Test
    fun testGenerateNestedProjectFile() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val classifier = InternalName.Classifier(listOf("com", "example"), listOf("TestClass"))
        val projects = mapOf(
            NameProject("test-project", dev.rnett.symbolexport.generator.ProjectCoordinates("com.example", "test-artifact", "1.0.0")) to
                    setOf(NameFromSourceSet("commonMain", classifier))
        )

        val result = symbolGenerator.generateNestedProjectFile(projects)

        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("Symbols from project `test-project` with coordinates `com.example:test-artifact:1.0.0`"))
        assertTrue(result.contains("mock-generated-content"))

        // Verify the mock was called with correct parameters
        assertEquals("test-project", mockGenerator.lastObjectName)
        assertEquals(setOf(NameFromSourceSet("commonMain", classifier)), mockGenerator.lastNames)
        assertEquals("Symbols from project `test-project` with coordinates `com.example:test-artifact:1.0.0`", mockGenerator.lastJavadocPrefix)
    }

    @Test
    fun testGenerateFlatProjectFile() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", true, mockGenerator)

        val classifier = InternalName.Classifier(listOf("com", "example"), listOf("TestClass"))
        val projects = mapOf(
            NameProject("test-project", dev.rnett.symbolexport.generator.ProjectCoordinates("com.example", "test-artifact", "1.0.0")) to
                    setOf(NameFromSourceSet("commonMain", classifier))
        )

        val result = symbolGenerator.generateFlatProjectFile(projects)

        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("Symbols from project `test-project` with coordinates `com.example:test-artifact:1.0.0`"))
        assertTrue(result.contains("End `test-project`"))
        assertTrue(result.contains("mock-generated-content"))

        // Verify the mock was called with correct parameters
        assertNull(mockGenerator.lastObjectName)
        assertEquals(setOf(NameFromSourceSet("commonMain", classifier)), mockGenerator.lastNames)
        assertNull(mockGenerator.lastJavadocPrefix)
    }

    @Test
    fun testGenerateSymbolsObject() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val classifier = InternalName.Classifier(listOf("com", "example"), listOf("TestClass"))
        val names = setOf(NameFromSourceSet("commonMain", classifier))

        val result = symbolGenerator.generateSymbolsObject("TestObject", names, "Test javadoc")

        assertEquals("mock-generated-content", result)

        // Verify the mock was called with correct parameters
        assertEquals("TestObject", mockGenerator.lastObjectName)
        assertEquals(names, mockGenerator.lastNames)
        assertEquals("Test javadoc", mockGenerator.lastJavadocPrefix)
    }

    @Test
    fun testGenerateSymbolsObjectWithNullObjectName() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val classifier = InternalName.Classifier(listOf("com", "example"), listOf("TestClass"))
        val names = setOf(NameFromSourceSet("commonMain", classifier))

        val result = symbolGenerator.generateSymbolsObject(null, names, null)

        assertEquals("mock-generated-content", result)

        // Verify the mock was called with correct parameters
        assertNull(mockGenerator.lastObjectName)
        assertEquals(names, mockGenerator.lastNames)
        assertNull(mockGenerator.lastJavadocPrefix)
    }

    @Test
    fun testGenerateSymbolsFileWithNullContent() {
        val mockGenerator = MockProjectObjectGenerator()
        mockGenerator.returnValue = null
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val entries = listOf(
            createTestEntry(name = InternalName.Classifier(listOf("com", "example"), listOf("TestClass")))
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        // When ProjectObjectGenerator returns null, it gets appended as "null" string
        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("null"))
    }

    @Test
    fun testGenerateSymbolsFileWithMultipleProjects() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val entries = listOf(
            createTestEntry(
                projectName = "project-a",
                group = "com.example.a",
                artifact = "artifact-a",
                version = "1.0.0",
                name = InternalName.Classifier(listOf("com", "example", "a"), listOf("ClassA"))
            ),
            createTestEntry(
                projectName = "project-b",
                group = "com.example.b",
                artifact = "artifact-b",
                version = "2.0.0",
                name = InternalName.Classifier(listOf("com", "example", "b"), listOf("ClassB"))
            )
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("Symbols from project `project-a` with coordinates `com.example.a:artifact-a:1.0.0`"))
        assertTrue(result.contains("Symbols from project `project-b` with coordinates `com.example.b:artifact-b:2.0.0`"))
    }

    @Test
    fun testGenerateSymbolsFileWithMultipleSourceSets() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val entries = listOf(
            createTestEntry(
                sourceSetName = "commonMain",
                name = InternalName.Classifier(listOf("com", "example"), listOf("CommonClass"))
            ),
            createTestEntry(
                sourceSetName = "jvmMain",
                name = InternalName.Classifier(listOf("com", "example"), listOf("JvmClass"))
            )
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
    }

    @Test
    fun testPreambleGeneration() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.custom.package", false, mockGenerator)

        val entries = listOf(
            createTestEntry(name = InternalName.Classifier(listOf("test"), listOf("TestClass")))
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("package com.custom.package"))
        assertTrue(result.contains("@file:Suppress(\"RemoveRedundantBackticks\", \"RedundantVisibilityModifier\", \"ClassName\")"))
        assertTrue(result.contains("import dev.rnett.symbolexport.symbol.*"))
        assertTrue(result.contains("import dev.rnett.symbolexport.symbol.Symbol.*"))
    }

    @Test
    fun testSymbolsCommentString() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val entries = listOf(
            createTestEntry(
                projectName = "my-project",
                group = "org.example",
                artifact = "my-artifact",
                version = "3.1.4",
                name = InternalName.Classifier(listOf("test"), listOf("TestClass"))
            )
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("Symbols from project `my-project` with coordinates `org.example:my-artifact:3.1.4`"))
    }

    @Test
    fun testGenerateSymbolsFileWithAnnotation() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        // Create an annotation
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "stringParam" to AnnotationParameterType.Primitive.STRING,
                "intParam" to AnnotationParameterType.Primitive.INT
            )
        )

        val entries = listOf(
            createTestEntry(
                name = annotation
            )
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("mock-generated-content"))

        // Verify the mock was called with annotation
        assertEquals(setOf(NameFromSourceSet("commonMain", annotation)), mockGenerator.lastNames)
    }

    @Test
    fun testGenerateSymbolsFileWithMixedSymbolsAndAnnotations() {
        val mockGenerator = MockProjectObjectGenerator()
        val symbolGenerator = SymbolGenerator("com.test", false, mockGenerator)

        val classifier = InternalName.Classifier(listOf("com", "example"), listOf("TestClass"))

        // Create an annotation
        val annotation = InternalName.Annotation(
            packageName = listOf("test", "package"),
            classNames = listOf("TestAnnotation"),
            parameters = mapOf(
                "stringParam" to AnnotationParameterType.Primitive.STRING,
                "intParam" to AnnotationParameterType.Primitive.INT
            )
        )

        val entries = listOf(
            createTestEntry(
                sourceSetName = "commonMain",
                name = classifier
            ),
            createTestEntry(
                sourceSetName = "commonMain",
                name = annotation
            )
        )

        val result = symbolGenerator.generateSymbolsFile(entries)

        assertTrue(result != null)
        assertTrue(result.contains("package com.test"))
        assertTrue(result.contains("internal object Symbols {"))
        assertTrue(result.contains("mock-generated-content"))

        // Verify the mock was called with both classifier and annotation
        val expectedNames = setOf(
            NameFromSourceSet("commonMain", classifier),
            NameFromSourceSet("commonMain", annotation)
        )
        assertEquals(expectedNames, mockGenerator.lastNames)
    }
}
