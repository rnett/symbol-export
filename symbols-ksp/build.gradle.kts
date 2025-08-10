plugins {
    id("build.kotlin-jvm")
    id("build.published-module")
}

dependencies {
    implementation(libs.ksp.api)
    api(project(":symbols"))
}