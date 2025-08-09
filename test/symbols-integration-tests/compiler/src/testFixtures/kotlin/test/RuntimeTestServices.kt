package test

import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

private val runtimeClasspath =
    System.getProperty("compilerTestRuntime.classpath")?.split(File.pathSeparator)?.map(::File)

class RuntimeEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        if (runtimeClasspath == null) return
        for (file in runtimeClasspath) {
            configuration.addJvmClasspathRoot(file)
        }
    }
}

class RuntimeClassPathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
    override fun runtimeClassPaths(module: TestModule): List<File> {
        return runtimeClasspath.orEmpty()
    }
}
