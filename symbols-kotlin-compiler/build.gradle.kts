plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

description = "Symbol-export integrations for working with symbols and Kotlin compiler types"

dependencies {
    implementation(kotlin("compiler"))
    api(project(":symbols"))
}