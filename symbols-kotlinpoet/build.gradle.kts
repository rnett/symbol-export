plugins {
    id("build.kotlin-jvm")
    id("build.published-module")
}

dependencies {
    implementation(libs.kotlinpoet)
    api(project(":symbols"))
}