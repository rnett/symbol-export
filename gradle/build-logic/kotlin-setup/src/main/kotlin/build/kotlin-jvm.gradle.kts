package build

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(Shared::toolchain)
    explicitApi()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(kotlin("test"))
}