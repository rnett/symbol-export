package dev.rnett.lattice.support

import dev.rnett.lattice.RuntimeClassPathProvider
import dev.rnett.lattice.RuntimeEnvironmentConfigurator
import dev.rnett.symbolexport.PluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives.IGNORE_DEXING
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.FULL_JDK
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.JVM_TARGET
import org.jetbrains.kotlin.test.directives.LanguageSettingsDirectives
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices

fun TestConfigurationBuilder.baseConfiguration() {
    useSourcePreprocessor(
        {
            ImportsPreprocessor(
                setOf(
                    "dev.rnett.symbolexport.*",
                ),
                it
            )
        }
    )
}

fun TestConfigurationBuilder.configurePlugin() {
    baseConfiguration()
    useConfigurators(
        ::BuildableExtensionRegistrarConfigurator,
        ::RuntimeEnvironmentConfigurator,
    )

    useCustomRuntimeClasspathProviders(
        ::RuntimeClassPathProvider,
    )

    defaultDirectives {
        JVM_TARGET.with(JvmTarget.JVM_17)
        +FULL_JDK
        +WITH_STDLIB
        +IGNORE_DEXING

        LanguageSettingsDirectives.LANGUAGE with "+ContextParameters"
    }
}

class BuildableExtensionRegistrarConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    @OptIn(ExperimentalCompilerApi::class)
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        FirExtensionRegistrarAdapter.registerExtension(PluginRegistrar(null, true))
    }
}
