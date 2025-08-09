plugins {
    id("build.kotlin-jvm")
    id("build.dokka")
}

dependencies {
    implementation(kotlin("compiler"))
    api(project(":symbols"))
}