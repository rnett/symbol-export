plugins {
    id("build.kotlin-jvm")
    id("build.public-module")
    id("java-gradle-plugin")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kcp.dev.gradle)
}

description = "The symbol-export export and import Gradle plugins"

// Wire the Gradle plugin module to the compiler plugin project for coordinates and metadata
compilerSupportPluginDevelopment {
    compilerPluginProjectPath = ":compiler-plugin"
}

dependencies {
    implementation(project(":names-internal"))
    implementation(project(":generator"))
}

tasks.shadowJar {
    archiveClassifier = ""
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
    }
    relocate("kotlinx.serialization", "dev.rnett.symbolexport.kotlinx.serialization")
}

tasks.jar {
    archiveClassifier = "single"
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
        create("SymbolExportExport2") {
            id = "dev.rnett.symbol-export.export2"
            displayName = "Symbol export - export2"
            description = "Allows the exporting of annotated symbols for reference by other modules."
            implementationClass = "dev.rnett.symbolexport.export.Export2Plugin"
        }
        create("SymbolExportImport") {
            id = "dev.rnett.symbol-export.import"
            displayName = "Symbol export - import"
            description = "Allows a project to consume symbols exposed by dev.rnett.symbol-exporter.export."
            implementationClass = "dev.rnett.symbolexport.import.ImportPlugin"
        }
    }
}