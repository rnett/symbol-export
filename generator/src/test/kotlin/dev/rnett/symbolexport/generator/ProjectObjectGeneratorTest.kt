package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProjectObjectGeneratorTest {

    @Test
    fun `test generate with null objectName and empty names`() {
        val generator = ProjectObjectGenerator(null, emptySet())
        val result = generator.generate(null)
        assertNull(result, "Result should be null for empty names")
    }

    @Test
    fun `test generate with objectName and empty names`() {
        val generator = ProjectObjectGenerator("TestProject", emptySet())
        val result = generator.generate(null)
        assertNull(result, "Result should be null for empty names even with objectName")
    }

    @Test
    fun `test generate with single source set`() {
        val names = setOf(
            NameFromSourceSet(
                "commonMain",
                InternalName.TopLevelMember(
                    packageName = listOf("dev", "rnett", "test"),
                    name = "topLevelFunction"
                )
            )
        )

        val generator = ProjectObjectGenerator("TestProject", names)
        val result = generator.generate(null)

        assertNotNull(result, "Result should not be null")
        assertTrue(result.contains("object TestProject {"), "Result should contain object declaration")
        assertTrue(result.contains("val dev_rnett_test_topLevelFunction: TopLevelMember"), "Result should contain symbol declaration")
    }

    @Test
    fun `test generate with multiple source sets`() {
        val names = setOf(
            NameFromSourceSet(
                "commonMain",
                InternalName.TopLevelMember(
                    packageName = listOf("dev", "rnett", "test"),
                    name = "commonFunction"
                )
            ),
            NameFromSourceSet(
                "jvmMain",
                InternalName.TopLevelMember(
                    packageName = listOf("dev", "rnett", "test"),
                    name = "jvmFunction"
                )
            )
        )

        val generator = ProjectObjectGenerator("TestProject", names)
        val result = generator.generate(null)

        assertNotNull(result, "Result should not be null")
        assertTrue(result.contains("object TestProject {"), "Result should contain object declaration")
        assertTrue(result.contains("object CommonMain {"), "Result should contain CommonMain object")
        assertTrue(result.contains("object JvmMain {"), "Result should contain JvmMain object")
        assertTrue(result.contains("val dev_rnett_test_commonFunction: TopLevelMember"), "Result should contain common symbol")
        assertTrue(result.contains("val dev_rnett_test_jvmFunction: TopLevelMember"), "Result should contain jvm symbol")
    }

    @Test
    fun `test generate with javadoc prefix`() {
        val names = setOf(
            NameFromSourceSet(
                "commonMain",
                InternalName.TopLevelMember(
                    packageName = listOf("dev", "rnett", "test"),
                    name = "topLevelFunction"
                )
            )
        )

        val generator = ProjectObjectGenerator("TestProject", names)
        val result = generator.generate("Test javadoc")

        assertNotNull(result, "Result should not be null")
        assertTrue(result.contains("/**"), "Result should contain javadoc start")
        assertTrue(result.contains(" * Test javadoc"), "Result should contain javadoc content")
        assertTrue(result.contains(" */"), "Result should contain javadoc end")
    }

    @Test
    fun `test generate with different symbol types`() {
        val names = setOf(
            // TopLevelMember
            NameFromSourceSet(
                "commonMain",
                InternalName.TopLevelMember(
                    packageName = listOf("dev", "rnett", "test"),
                    name = "topLevelFunction"
                )
            ),
            // Classifier
            NameFromSourceSet(
                "commonMain",
                InternalName.Classifier(
                    packageName = listOf("dev", "rnett", "test"),
                    classNames = listOf("TestClass")
                )
            ),
            // ClassifierMember
            NameFromSourceSet(
                "commonMain",
                InternalName.ClassifierMember(
                    classifier = InternalName.Classifier(
                        packageName = listOf("dev", "rnett", "test"),
                        classNames = listOf("TestClass")
                    ),
                    name = "classMethod"
                )
            ),
            // Constructor
            NameFromSourceSet(
                "commonMain",
                InternalName.Constructor(
                    classifier = InternalName.Classifier(
                        packageName = listOf("dev", "rnett", "test"),
                        classNames = listOf("TestClass")
                    ),
                    name = "<init>"
                )
            ),
            // EnumEntry
            NameFromSourceSet(
                "commonMain",
                InternalName.EnumEntry(
                    owner = InternalName.Classifier(
                        packageName = listOf("dev", "rnett", "test"),
                        classNames = listOf("TestEnum")
                    ),
                    name = "ENTRY",
                    ordinal = 0
                )
            )
        )

        val generator = ProjectObjectGenerator("TestProject", names)
        val result = generator.generate(null)

        assertNotNull(result, "Result should not be null")
        assertTrue(result.contains("val dev_rnett_test_topLevelFunction: TopLevelMember"), "Result should contain top level member")
        assertTrue(result.contains("val dev_rnett_test_TestClass: Classifier"), "Result should contain classifier")
        assertTrue(result.contains("val dev_rnett_test_TestClass_classMethod: ClassifierMember"), "Result should contain classifier member")
        assertTrue(result.contains("val dev_rnett_test_TestClass_init: Constructor"), "Result should contain constructor")
        assertTrue(result.contains("val dev_rnett_test_TestEnum_ENTRY: EnumEntry"), "Result should contain enum entry")
    }
}
