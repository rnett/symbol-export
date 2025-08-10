plugins {
    id("build.kotlin-jvm")
    id("build.published-module")
}

dependencies {
    implementation(kotlin("compiler"))
    api(project(":symbols"))
}