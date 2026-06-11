package build

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

extensionIfPresent<KotlinMultiplatformExtension> {
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        keepLocallyUnsupportedTargets = true
    }

    sourceSets.configureEach {
        explicitApi()
    }
    explicitApi()
}

extensionIfPresent<KotlinJvmExtension>() {
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}
tasks.named("check") {
    dependsOn("checkLegacyAbi")
}