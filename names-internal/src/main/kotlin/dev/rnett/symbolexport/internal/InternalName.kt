package dev.rnett.symbolexport.internal

import kotlinx.serialization.Serializable

@Serializable
public data class ProjectCoordinates(val group: String, val artifact: String, val version: String)

@Serializable
public data class InternalNameEntry(
    val projectName: String,
    val projectCoordinates: ProjectCoordinates,
    val sourceSetName: String,
    val name: InternalName
)

@Serializable
public sealed interface InternalName {

    /**
     * A class, interface, object, etc.
     */
    @Serializable
    public data class Classifier(val packageName: List<String>, val classNames: List<String>) : InternalName

    @Serializable
    public data class ClassifierMember(val classifier: Classifier, val name: String) : InternalName

    @Serializable
    public data class TopLevelMember(val packageName: List<String>, val name: String) : InternalName
}