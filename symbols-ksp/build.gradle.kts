plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

description = "Symbol-export integrations for working with symbols and KSP"

dependencies {
    implementation(libs.ksp.api)
    api(project(":symbols"))
}