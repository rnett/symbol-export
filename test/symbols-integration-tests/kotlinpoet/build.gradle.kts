plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.rnett.symbol-export.import")
}


dependencies {
    importSymbols(project(":symbols-integration-tests:test-symbols"))
    testImplementation(project(":symbols-integration-tests:test-symbols"))

    testImplementation(libs.kotlinpoet)

    testImplementation("dev.rnett.symbol-export:symbols-kotlinpoet")

    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
}