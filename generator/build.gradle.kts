import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

plugins {
    id("build.kotlin-jvm")
}

dependencies {
    implementation(project(":names-internal"))
}

kotlin {
    explicitApi = ExplicitApiMode.Disabled
}