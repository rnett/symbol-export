plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

dependencies {
    implementation(kotlin("compiler"))
    api(project(":symbols"))
}