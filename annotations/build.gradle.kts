import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    id("build.kotlin-multiplatform")
    id("build.public-module")
}

dokka {
    dokkaSourceSets.configureEach {
        reportUndocumented = true
        documentedVisibilities.add(VisibilityModifier.Public)
        documentedVisibilities.add(VisibilityModifier.Protected)
    }
}