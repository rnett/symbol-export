package dev.rnett.symbolexport.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// want to avoid serialization breaking if I relocate the package
@Serializable
@SerialName("InternalName")
public sealed interface InternalName {

    /**
     * A class, interface, object, etc.
     */
    @Serializable
    @SerialName("Classifier")
    public data class Classifier(val packageName: List<String>, val classNames: List<String>) : InternalName

    @Serializable
    @SerialName("ClassifierMember")
    public data class ClassifierMember(val classifier: Classifier, val name: String) : InternalName

    @Serializable
    @SerialName("TopLevelMember")
    public data class TopLevelMember(val packageName: List<String>, val name: String) : InternalName
}