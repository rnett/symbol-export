plugins {
    id("build.kotlin-jvm")
    id("build.dokka")
}

dependencies {
    implementation(libs.ksp.api)
    api(project(":symbols"))
}