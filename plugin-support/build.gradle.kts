plugins {
    id("build.kotlin-jvm")
    id("build.public-abi")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":names-internal"))
    implementation(project(":symbols"))
    implementation(libs.kotlinpoet)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
    systemProperty("updateSnapshots", providers.systemProperty("updateSnapshots").getOrElse("false"))
}