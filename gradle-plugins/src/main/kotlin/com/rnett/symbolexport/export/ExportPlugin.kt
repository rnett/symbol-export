package com.rnett.symbolexport.export

import com.rnett.symbolexport.BuildConfig
import com.rnett.symbolexport.Shared
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused")
// Used via reflection.
public class ExportPlugin : KotlinCompilerPluginSupportPlugin {
    internal companion object {
        val CONFIGURATION_NAME = "exportedSymbols"
    }

    override fun apply(target: Project) {
        target.extensions.create("symbolExport", ExportExtension::class.java).apply {
            symbolExportOutputDirectory.convention(target.layout.buildDirectory.dir("symbol-export"))
            autoAddAnnotationDependency.convention(true)
        }

        val nameListConfiguration = target.configurations.register(CONFIGURATION_NAME) {
            it.attributes {
                it.attribute(Usage.USAGE_ATTRIBUTE, Shared.USAGE_ATTRIBUTE_VALUE)
            }
            it.isCanBeResolved = false
        }

    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        "test" !in kotlinCompilation.name.lowercase()

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
        version = BuildConfig.KOTLIN_PLUGIN_VERSION,
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.extensions.getByType(ExportExtension::class.java)

        if (extension.autoAddAnnotationDependency.getOrElse(false)) {
            kotlinCompilation.dependencies { implementation(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES) }
            kotlinCompilation.defaultSourceSet.dependencies {
                implementation(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES)
            }
        }

        val file =
            extension.symbolExportOutputDirectory.file("${Shared.SYMBOLS_FILE_PREFIX}${kotlinCompilation.defaultSourceSet.name}${Shared.SYMBOLS_FILE_EXTENSION}")

        kotlinCompilation.compileTaskProvider.configure {
            it.outputs.file(file)
                .withPropertyName("symbolExportOutputFile").optional()
        }

        project.artifacts {
            it.add(CONFIGURATION_NAME, kotlinCompilation.compileTaskProvider.flatMap { file }) {
                it.builtBy(kotlinCompilation.compileTaskProvider)
            }
        }

        return project.provider {

            listOf(
                SubpluginOption(
                    "symbolExportOutputFilePath",
                    file.get().asFile.absolutePath
                )
            )
        }
    }
}