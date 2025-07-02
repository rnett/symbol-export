package build

import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec

internal object Shared {
    fun toolchain(spec: JavaToolchainSpec) = with(spec) {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}