plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
}

description = "Symbol-export integrations for working with symbols and KotlinPoet"

dependencies {
    implementation(libs.kotlinpoet)
    api(project(":symbols"))
}