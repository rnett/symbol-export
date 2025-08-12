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

    public val qualifiedName: String

    @Serializable
    public sealed interface Member : InternalName

    /**
     * A class, interface, object, etc.
     */
    @Serializable
    public data class Classifier(val packageName: List<String>, val classNames: List<String>) : InternalName {
        override val qualifiedName: String by lazy { packageName.joinToString(".") + "." + classNames.joinToString(".") }
    }

    @Serializable
    public data class ClassifierMember(val classifier: Classifier, val name: String) : Member {
        override val qualifiedName: String by lazy { "${classifier.qualifiedName}.$name" }
    }

    @Serializable
    public data class TopLevelMember(val packageName: List<String>, val name: String) : Member {
        override val qualifiedName: String by lazy { packageName.joinToString(".") + "." + name }
    }

    @Serializable
    public data class Constructor(val classifier: Classifier, val name: String) : Member {
        override val qualifiedName: String by lazy { "${classifier.qualifiedName}.$name" }
    }

    @Serializable
    public data class ReceiverParameter(
        val owner: Member,
        val name: String,
        val index: Int,
        @SerialName("parameterType")
        val type: Type
    ) : InternalName {

        override val qualifiedName: String by lazy { "${owner.qualifiedName}.$name" }

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
        override val qualifiedName: String by lazy { "${owner.qualifiedName}.$name" }

        @Serializable
        public enum class Type : ParameterType {
            VALUE, CONTEXT;
        }
    }

    @Serializable
    public data class TypeParameter(val owner: InternalName, val name: String, val index: Int) : InternalName {
        override val qualifiedName: String by lazy { "${owner.qualifiedName}.$name" }
    }

    @Serializable
    public data class EnumEntry(val owner: Classifier, val name: String, val ordinal: Int) : InternalName {
        override val qualifiedName: String by lazy { "${owner.qualifiedName}.$name" }
    }

    @Serializable
    public data class Annotation(
        val packageName: List<String>,
        val classNames: List<String>,
        val parameters: List<Parameter>
    ) : InternalName {
        override val qualifiedName: String by lazy { packageName.joinToString(".") + "." + classNames.joinToString(".") }

        @Serializable
        public data class Parameter(val name: String, val index: Int, val type: AnnotationParameterType)
    }
}