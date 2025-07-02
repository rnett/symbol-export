plugins {
    id("build.kotlin-jvm")
}

dependencies {
    implementation(libs.kotlinpoet)
    api(project(":symbols"))
}