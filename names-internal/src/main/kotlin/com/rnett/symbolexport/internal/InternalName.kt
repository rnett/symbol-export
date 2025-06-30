package com.rnett.symbolexport.internal

import kotlinx.serialization.Serializable

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