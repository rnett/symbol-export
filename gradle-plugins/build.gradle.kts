plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
    id("java-gradle-plugin")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.shadow)
}

description = "The symbol-export export and import Gradle plugins"

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("gradle-plugin-api"))

    implementation(project(":names-internal"))
    implementation(project(":generator"))
}

dokka {
    dokkaPublications.configureEach {
        includes.from("Module.md")
    }
}

tasks.shadowJar {
    archiveClassifier = ""
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
    }
    relocate("kotlinx.serialization", "dev.rnett.symbolexport.kotlinx.serialization")
}

fun libraryCoordinates(project: Project) = buildString {
    append(group)
    append(":")
    append(project.name)
    val versionString = version.toString().ifEmpty { null }?.takeUnless { it == "undefined" }
    if (versionString != null) {
        append(":")
        append(versionString)
    }
}

buildConfig {
    packageName("dev.rnett.symbolexport")

    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")

    val pluginProject = project(":compiler-plugin")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${pluginProject.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${pluginProject.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${pluginProject.version}\"")

    buildConfigField(
        type = "String",
        name = "ANNOTATIONS_LIBRARY_COORDINATES",
        expression = "\"${libraryCoordinates(project(":annotations"))}\""
    )

    buildConfigField(
        type = "String",
        name = "SYMBOLS_LIBRARY_COORDINATES",
        expression = "\"${libraryCoordinates(project(":symbols"))}\""
    )
}

gradlePlugin {
    plugins {
        create("SymbolExportExport") {
            id = "dev.rnett.symbol-export.export"
            displayName = "Symbol export - export"
            description = "Allows the exporting of annotated symbols for reference by other modules."
            implementationClass = "dev.rnett.symbolexport.export.ExportPlugin"
        }
        create("SymbolExportImport") {
            id = "dev.rnett.symbol-export.import"
            displayName = "Symbol export - import"
            description = "Allows a project to consume symbols exposed by dev.rnett.symbol-exporter.export."
            implementationClass = "dev.rnett.symbolexport.import.ImportPlugin"
        }
    }
}