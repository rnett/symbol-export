package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.generator.ProjectCoordinates.Companion.toProjectCoordinates
import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalNameEntry


internal data class NameProject(
    val projectName: String,
    val projectCoordinates: ProjectCoordinates,
) {
    companion object {
        fun InternalNameEntry.toNameProject() = NameProject(projectName, projectCoordinates.toProjectCoordinates())
    }
}

internal data class ProjectCoordinates(val group: String, val artifact: String, val version: String) {
    companion object {
        fun dev.rnett.symbolexport.internal.ProjectCoordinates.toProjectCoordinates() =
            ProjectCoordinates(group, artifact, version)
    }
}

internal data class NameFromSourceSet(val sourceSet: String, val name: InternalName) {
    companion object {
        fun InternalNameEntry.toNameFromSourceSet() = NameFromSourceSet(sourceSetName, name)
    }
}

internal data class NameSourceSet(val name: String) {
    fun objectName() = name.replaceFirstChar { it.uppercase() }

    companion object {
        val COMMON_MAIN = NameSourceSet("commonMain")
    }
}
