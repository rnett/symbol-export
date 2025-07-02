plugins {
    id("build.kotlin-jvm")
}

dependencies {
    implementation(project(":names-internal"))
    implementation(libs.kotlinx.serialization.json)
}