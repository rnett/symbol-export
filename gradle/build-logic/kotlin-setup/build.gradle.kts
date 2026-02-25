plugins {
    `kotlin-dsl`
}

dependencies {
    api(libs.kotlin.gradle.plugin)
    api(libs.kotlin.power.assert.gradle.plugin)
    api(libs.dokka.plugin)
    api(libs.publishing.plugin)
}