package build

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationExtension
import org.jetbrains.kotlin.gradle.dsl.abi.AbiValidationMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

extensionIfPresent<KotlinMultiplatformExtension> {
    @OptIn(ExperimentalAbiValidation::class)
    extensionIfPresent<AbiValidationMultiplatformExtension> {
        enabled = true
        klib {
            enabled = true
            keepUnsupportedTargets = true
        }
    }

    sourceSets.configureEach {
        explicitApi()
    }
    explicitApi()
}

extensionIfPresent<KotlinJvmExtension>() {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    extensionIfPresent<AbiValidationExtension> {
        enabled = true
    }
}
tasks.named("check") {
    dependsOn("checkLegacyAbi")
}