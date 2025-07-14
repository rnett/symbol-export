package dev.rnett.lattice.tests

import dev.rnett.lattice.ClasspathBasedStandardLibrariesPathProvider
import dev.rnett.lattice.support.configurePlugin
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.AbstractFirLightTreeDiagnosticsTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class BaseDiagnosticCompilerTest : AbstractFirLightTreeDiagnosticsTest() {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return ClasspathBasedStandardLibrariesPathProvider
    }

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        builder.configurePlugin()
    }
}