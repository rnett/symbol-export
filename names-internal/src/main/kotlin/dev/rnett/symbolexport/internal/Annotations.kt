package dev.rnett.symbolexport.internal

import kotlinx.serialization.Serializable

@Serializable
public sealed interface AnnotationParameterType {
    @Serializable
    public data object KClass : AnnotationParameterType

    @Serializable
    public data class Enum(val enumClass: InternalName.Classifier) : AnnotationParameterType

    @Serializable
    public data class Annotation(val annotationClass: InternalName.Classifier) : AnnotationParameterType

    @Serializable
    public data class Array(val elementType: AnnotationParameterType) : AnnotationParameterType

    @Serializable
    public enum class Primitive : AnnotationParameterType {
        STRING, BOOLEAN, INT, FLOAT, LONG, DOUBLE, CHAR, BYTE, SHORT;
    }
}