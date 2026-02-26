package dev.rnett.symbolexport.tests

import dev.rnett.kcp.development.testing.generation.BaseTestGenerator
import dev.rnett.kcp.development.testing.generation.TestGenerationBuilder
import dev.rnett.kcp.development.testing.generation.addLevel
import dev.rnett.kcp.development.testing.generation.withTestSpec
import dev.rnett.kcp.development.testing.tests.levels.TestLevel
import dev.rnett.symbolexport.PluginComponentRegistrar
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives.LANGUAGE

/**
 * Test generator for the symbol-export compiler plugin using kcp-development test support.
 * Generates tests for folders under src/testData.
 */
object TestGenerator : BaseTestGenerator() {
    override val testsRootPackage: String = "dev.rnett.symbolexport.tests.generated"

    // Add imports used by test data files
    override val imports: Set<String> = setOf(
        "dev.rnett.symbolexport.*",
        "dev.rnett.symbolexport.reference.*",
    )

    override fun TestGenerationBuilder.generateTests() {
        withTestSpec(PluginComponentRegistrar::class, PluginComponentRegistrar.Spec(warnOnExported = true, outputDir = null, rootPath = null))
        // Global compiler test configuration needed by our test data
        configure {
            defaultDirectives {
                // Enable context parameters language feature used in tests
                LANGUAGE with "+ContextParameters"
                // Avoid dexing in JVM tests
                +IGNORE_DEXING
            }
        }
        // Mirror existing groups from the previous generator
        group("export") {
            addLevel(TestLevel.Diagnostics)
            tests()
        }
        group("errors") {
            addLevel(TestLevel.Diagnostics)
            tests()
        }
        group("reference") {
            group("export") {
                addLevel(TestLevel.Diagnostics)
                tests()
            }
            group("errors") {
                addLevel(TestLevel.Diagnostics)
                tests()
            }
        }
    }
}
