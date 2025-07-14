package dev.rnett.lattice

import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

private val buildableRuntimeClasspath =
    System.getProperty("compilerTestRuntime.classpath")?.split(File.pathSeparator)?.map(::File)
        ?: error("Unable to get a valid classpath from 'compilerTestRuntime.classpath' property")

class RuntimeEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {
    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        for (file in buildableRuntimeClasspath) {
            configuration.addJvmClasspathRoot(file)
        }
    }
}

class RuntimeRuntimeClassPathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
    override fun runtimeClassPaths(module: TestModule): List<File> {
        return buildableRuntimeClasspath
    }
}
