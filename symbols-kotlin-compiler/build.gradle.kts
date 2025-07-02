plugins {
    id("build.kotlin-jvm")
}

dependencies {
    implementation(kotlin("compiler"))
    api(project(":symbols"))
}