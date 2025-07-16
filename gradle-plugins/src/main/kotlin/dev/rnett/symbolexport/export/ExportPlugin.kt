package dev.rnett.symbolexport.export

import dev.rnett.symbolexport.BuildConfig
import dev.rnett.symbolexport.Shared
import dev.rnett.symbolexport.Shared.EXPORTED_SYMBOLS_FILENAME
import dev.rnett.symbolexport.withDisallowedChanges
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*
import org.slf4j.LoggerFactory
import java.io.File

@Suppress("unused")
// Used via reflection.
public class ExportPlugin : KotlinCompilerPluginSupportPlugin {
    private val logger = LoggerFactory.getLogger(ExportPlugin::class.java)

    internal companion object {
        internal const val CONFIGURATION_NAME = "exportedSymbols"
    }

    private object PluginParameters {
        // from CommandLineProcessor

        const val OUTPUT_FILE = "outputFile"
        const val PROJECT_NAME = "projectName"
        const val PROJECT_GROUP = "projectGroup"
        const val PROJECT_ARTIFACT = "projectArtifact"
        const val PROJECT_VERSION = "projectVersion"
        const val SOURCE_SET_NAME = "sourceSetName"
    }

    override fun apply(target: Project) {
        target.extensions.create("symbolExport", ExportExtension::class.java).apply {
            symbolExportOutputDirectory.convention(target.layout.buildDirectory.dir("symbol-export"))
            autoAddAnnotationDependency.convention(true)
            ignoreSourceSets.convention(emptySet())
            exportFromSourceSets.convention(emptySet())
            projectSymbolsName.convention(target.provider { target.name })
        }

        val nameListConfiguration = target.configurations.register(CONFIGURATION_NAME) {
            it.attributes {
                it.attribute(
                    Usage.USAGE_ATTRIBUTE,
                    target.objects.named(Usage::class.java, Shared.USAGE_ATTRIBUTE_VALUE)
                )
            }
            it.isCanBeResolved = false
        }
    }

    private val KotlinCompilation<*>.symbolExtension
        get() = this.project.extensions.getByType(ExportExtension::class.java)

    private val KotlinCompilation<*>.symbolExportDir: Provider<File>
        get() = symbolExtension.symbolExportOutputDirectory.asFile.map { it.resolve("${defaultSourceSet.name}") }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        if ("test" in kotlinCompilation.name.lowercase()) {
            return false
        }

        setupCompilation(kotlinCompilation)

        val sourceSetName = kotlinCompilation.defaultSourceSet.name

        val exportFrom =
            kotlinCompilation.symbolExtension.exportFromSourceSets.withDisallowedChanges().orNull?.ifEmpty { null }
        val inExport = exportFrom == null || sourceSetName in exportFrom

        val doExport =
            inExport && sourceSetName !in kotlinCompilation.symbolExtension.ignoreSourceSets.withDisallowedChanges()
                .get()
        if (!doExport) {
            logger.info(
                "Ignoring symbols from compilation {} @ {}",
                kotlinCompilation.name,
                kotlinCompilation.target.name
            )
        }
        return doExport
    }

    private val alreadySetup = mutableSetOf<String>()

    private fun setupCompilation(kotlinCompilation: KotlinCompilation<*>) {
        if (!alreadySetup.add(kotlinCompilation.compileTaskProvider.name)) {
            return
        }
        logger.debug(
            "Set up symbol export for compilation {} @ {} (symbols may still be ignored)",
            kotlinCompilation.name,
            kotlinCompilation.target.name
        )

        val dir = kotlinCompilation.symbolExportDir

        kotlinCompilation.compileTaskProvider.configure {
            it.outputs.dir(dir)
                .optional()
                .withPropertyName("symbolExportOutputDirectory")
        }
    }

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
        version = BuildConfig.KOTLIN_PLUGIN_VERSION,
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.project
        logger.info(
            "Applying symbol export plugin to compilation {} @ {}",
            kotlinCompilation.name,
            kotlinCompilation.target.name
        )

        if (kotlinCompilation.symbolExtension.autoAddAnnotationDependency.withDisallowedChanges().getOrElse(false)) {
            kotlinCompilation.dependencies {
                implementation(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES)
            }
            kotlinCompilation.defaultSourceSet.dependencies {
                implementation(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES)
            }
        }

        kotlinCompilation.symbolExtension.apply {
            symbolExportOutputDirectory.disallowChanges()

        }
        val file = kotlinCompilation.symbolExportDir.map { it.resolve(EXPORTED_SYMBOLS_FILENAME) }

        project.artifacts {
            it.add(CONFIGURATION_NAME, kotlinCompilation.compileTaskProvider.flatMap { file }) {
                it.builtBy(kotlinCompilation.compileTaskProvider)
            }
        }

        return project.provider {
            listOf(
                FilesSubpluginOption(
                    PluginParameters.OUTPUT_FILE,
                    listOf(file.get()),
                    kind = FilesOptionKind.INTERNAL, // the only available option for now, see https://youtrack.jetbrains.com/issue/KT-41711/Allow-FilesSubpluginOption-to-treat-files-as-inputs-in-Gradle-subplugins
                ),
                SubpluginOption(
                    PluginParameters.PROJECT_NAME,
                    kotlinCompilation.symbolExtension.projectSymbolsName.get()
                ),
                SubpluginOption(
                    PluginParameters.PROJECT_GROUP,
                    project.group.toString()
                ),
                SubpluginOption(
                    PluginParameters.PROJECT_ARTIFACT,
                    project.name
                ),
                SubpluginOption(
                    PluginParameters.PROJECT_VERSION,
                    project.version.toString()
                ),
                SubpluginOption(
                    PluginParameters.SOURCE_SET_NAME,
                    kotlinCompilation.defaultSourceSet.name
                )
            )
        }
    }
}