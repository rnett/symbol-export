plugins {
    id("build.kotlin-jvm")
    id("build.dokka")
}

dependencies {
    implementation(libs.kotlinpoet)
    api(project(":symbols"))
}