package dev.rnett.symbolexport.internal

import kotlinx.serialization.SerialName
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
public sealed interface ParameterType {

}

@Serializable
public sealed interface InternalName {

    @Serializable
    public sealed interface Member : InternalName

    /**
     * A class, interface, object, etc.
     */
    @Serializable
    public data class Classifier(val packageName: List<String>, val classNames: List<String>) : InternalName

    @Serializable
    public data class ClassifierMember(val classifier: Classifier, val name: String) : Member

    @Serializable
    public data class TopLevelMember(val packageName: List<String>, val name: String) : Member

    @Serializable
    public data class Constructor(val classifier: Classifier, val name: String) : Member

    @Serializable
    public data class ReceiverParameter(
        val owner: Member,
        val name: String,
        val index: Int,
        @SerialName("parameterType")
        val type: Type
    ) :
        InternalName {
        @Serializable
        public enum class Type : ParameterType {
            DISPATCH, EXTENSION;
        }
    }

    @Serializable
    public data class IndexedParameter(
        val owner: Member,
        val name: String,
        val index: Int,
        val indexInList: Int,
        @SerialName("parameterType")
        val type: Type
    ) : InternalName {
        @Serializable
        public enum class Type : ParameterType {
            VALUE, CONTEXT;
        }
    }

    @Serializable
    public data class TypeParameter(val owner: InternalName, val name: String, val index: Int) : InternalName

    @Serializable
    public data class EnumEntry(val owner: Classifier, val name: String, val ordinal: Int) : InternalName
}