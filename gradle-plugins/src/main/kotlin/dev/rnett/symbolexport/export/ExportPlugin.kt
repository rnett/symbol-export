package dev.rnett.symbolexport.export

import dev.rnett.symbolexport.BuildConfig
import dev.rnett.symbolexport.Shared
import dev.rnett.symbolexport.import.ImportPlugin
import dev.rnett.symbolexport.withDisallowedChanges
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.FilesOptionKind
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.slf4j.LoggerFactory
import java.io.File

/**
 * The export plugin - `dev.rnett.symbol-export.export`.
 *
 * Exports symbols according to [ExportExtension] (`symbolExport`).
 *
 * It does this by registering an outgoing configuration with a custom usage attribute value.
 * When you compile your kotlin code, JSON files of exported symbols are generated and added to that outgoing configuration.
 * This configuration can then be consumed by other projects using the [ImportPlugin].
 *
 * Also automatically adds a dependency on the symbol-export annotation library (see [ExportExtension.autoAddAnnotationDependency]).
 *
 * @see ExportExtension
 * @see ImportPlugin
 */
public class ExportPlugin : KotlinCompilerPluginSupportPlugin {
    private val logger = LoggerFactory.getLogger(ExportPlugin::class.java)

    internal companion object {
        internal const val CONFIGURATION_NAME = "exportedSymbols"
    }

    private object PluginParameters {
        // from CommandLineProcessor

        const val OUTPUT_DIR = "outputDir"
        const val PROJECT_NAME = "projectName"
        const val PROJECT_GROUP = "projectGroup"
        const val PROJECT_ARTIFACT = "projectArtifact"
        const val PROJECT_VERSION = "projectVersion"
        const val SOURCE_SET_NAME = "sourceSetName"
        const val ROOT_DIR_NAME = "rootDir"
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create("symbolExport", ExportExtension::class.java).apply {
            symbolExportOutputDirectory.convention(target.layout.buildDirectory.dir("symbol-export"))
            autoAddAnnotationDependency.convention(true)
            projectSymbolsName.convention(target.provider { target.name })
        }

        val helperConfiguration = target.configurations.register("symbolExportRunner") {
            it.isCanBeConsumed = true
        }

        target.dependencies.add(
            helperConfiguration.name,
            "${BuildConfig.KOTLIN_PLUGIN_GROUP}:plugin-support:${BuildConfig.KOTLIN_PLUGIN_VERSION}"
        )

        val nameListConfiguration = target.configurations.register(CONFIGURATION_NAME) {
            it.attributes {
                it.attribute(
                    Usage.USAGE_ATTRIBUTE,
                    target.objects.named(Usage::class.java, Shared.USAGE_ATTRIBUTE_VALUE)
                )
            }
            it.isCanBeResolved = false
            it.isCanBeConsumed = true
            // by default, the capability is group:artifact, leading to resolving to the latest version.
            // we don't want that
//            it.outgoing.capability(
//                Shared.getSymbolCapability(
//                    target.group.toString(),
//                    target.name,
//                    target.version.toString()
//                )
//            )
        }

        val postprocessTask = target.tasks.register("postProcessExportedSymbols", PostProcessSymbolsTask::class.java) {
            it.group = "symbol export"
            it.symbols.set(extension.symbolExportOutputDirectory.map { it.dir("raw") })
            it.outputFile.set(extension.symbolExportOutputDirectory.map { it.dir("processed").file("processed.json") })
            it.classpath.from(helperConfiguration)
            it.projectSymbolName.set(extension.projectSymbolsName)
        }

        target.tasks.named("assemble") {
            it.dependsOn(postprocessTask)
        }

        target.artifacts {
            it.add(nameListConfiguration.name, postprocessTask.flatMap { it.outputFile }) {
                it.builtBy(postprocessTask)
            }
        }
    }

    private val KotlinCompilation<*>.symbolExtension
        get() = this.project.extensions.getByType(ExportExtension::class.java)

    private val KotlinCompilation<*>.symbolExportDir: Provider<File>
        get() = symbolExtension.symbolExportOutputDirectory.asFile.map { it.resolve("raw").resolve(defaultSourceSet.name) }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        if (kotlinCompilation.name.endsWith("Test")) {
            return false
        }

        setupCompilation(kotlinCompilation)


        return true
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
        kotlinCompilation.project.tasks.named("postProcessExportedSymbols") {
            it.dependsOn(kotlinCompilation.compileTaskProvider)
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
            kotlinCompilation.defaultSourceSet.dependencies {
                implementation(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES)
            }
        }

        kotlinCompilation.symbolExtension.apply {
            symbolExportOutputDirectory.disallowChanges()

        }
        val file = kotlinCompilation.symbolExportDir
        kotlinCompilation.target.targetName

        return project.provider {
            listOf(
                FilesSubpluginOption(
                    PluginParameters.OUTPUT_DIR,
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
                ),
                FilesSubpluginOption(
                    PluginParameters.ROOT_DIR_NAME,
                    listOf(project.projectDir),
                    kind = FilesOptionKind.INTERNAL, // we do want this to be internal
                )
            )
        }
    }
}