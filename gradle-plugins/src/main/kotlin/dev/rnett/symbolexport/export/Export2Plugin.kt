package dev.rnett.symbolexport.export

import dev.rnett.symbolexport.BuildConfig
import dev.rnett.symbolexport.KotlinPluginType
import dev.rnett.symbolexport.kotlinExtension
import dev.rnett.symbolexport.whenKotlinPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer

public class Export2Plugin : Plugin<Project> {
    override fun apply(target: Project) {
        val setup = target.setupProject()
        context(target, setup) {
            target.whenKotlinPlugin {
                when (it) {
                    KotlinPluginType.Android -> configureAndroid(target.kotlinExtension as KotlinAndroidExtension)
                    KotlinPluginType.Jvm -> configureJvm(target.kotlinExtension as KotlinJvmExtension)
                    KotlinPluginType.Multiplatform -> configureMultiplatform(target.kotlinExtension as KotlinTargetsContainer)
                }
            }
        }
    }

    private fun Project.setupProject(): ProjectSetup {
        val config = configurations.create("symbolExportAnalyzer") {
            it.isCanBeResolved = true
            it.attributes {
                it.attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling::class.java, Bundling.SHADOWED))
            }
        }

        dependencies.add(config.name, "${BuildConfig.KOTLIN_PLUGIN_GROUP}:symbol-analyzer:${BuildConfig.KOTLIN_PLUGIN_VERSION}")

        return ProjectSetup(config)
    }

    private data class ProjectSetup(val analyzerConfiguration: Configuration)

    context(project: Project, setup: ProjectSetup)
    private fun configureAndroid(kotlin: KotlinAndroidExtension) {
        val task = registerExportTask(kotlin.target, "exportSymbols")
        setupRootTask(task)
    }

    context(project: Project, setup: ProjectSetup)
    private fun configureJvm(kotlin: KotlinJvmExtension) {
        val task = registerExportTask(kotlin.target, "exportSymbols")
        setupRootTask(task)
    }

    context(project: Project, setup: ProjectSetup)
    private fun configureMultiplatform(kotlin: KotlinTargetsContainer) {
        val targetTasks = kotlin.targets.map { target ->
            registerExportTask(target, "exportSymbols" + target.name.capitalized())
        }
        val rootTask = project.tasks.register("exportSymbols") {
            it.group = "symbol export"
            targetTasks.forEach { targetTask ->
                it.dependsOn(targetTask)
            }
        }
        setupRootTask(rootTask)
    }

    context(project: Project, setup: ProjectSetup)
    private fun setupRootTask(taskProvider: TaskProvider<*>) {
        project.tasks.named("assemble").configure {
            it.dependsOn(taskProvider)
        }
    }

    context(project: Project, setup: ProjectSetup)
    private fun registerExportTask(target: KotlinTarget, name: String): TaskProvider<*> {
        val main = target.compilations.named("main")
        val outputDir = project.layout.buildDirectory.dir("symbol-export/exported/${target.name}")
        val task = project.tasks.register(name, Export2Task::class.java) { task ->
            task.projectName.set(project.name)
            task.projectGroup.set(project.group.toString())
            task.projectVersion.set(project.version.toString())
            task.targetName.set(target.name)
            task.sourceSetName.set(main.map { it.defaultSourceSet.name })
            task.analysisClasspath.from(main.map { it.compileDependencyFiles })
            task.sources.from(main.map { it.defaultSourceSet.allKotlinSources })
            task.outputDir.set(outputDir)

            task.runnerClasspath.from(setup.analyzerConfiguration)
        }

        return task
    }
}