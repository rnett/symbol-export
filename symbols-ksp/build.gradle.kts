plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

dependencies {
    implementation(libs.ksp.api)
    api(project(":symbols"))
}