plugins {
    id("build.kotlin-jvm")
}

dependencies {
    implementation(libs.ksp.api)
    api(project(":symbols"))
}