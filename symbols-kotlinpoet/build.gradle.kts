plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

dependencies {
    implementation(libs.kotlinpoet)
    api(project(":symbols"))
}