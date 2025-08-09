package compilertest.tests

import compilertest.ClasspathBasedStandardLibrariesPathProvider
import compilertest.support.configurePlugin
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.runners.codegen.AbstractFirLightTreeBlackBoxCodegenTest
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class BaseCompilerTest : AbstractFirLightTreeBlackBoxCodegenTest() {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return ClasspathBasedStandardLibrariesPathProvider
    }

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        builder.configurePlugin()
    }
}