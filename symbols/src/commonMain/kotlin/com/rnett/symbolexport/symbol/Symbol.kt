package com.rnett.symbolexport.symbol

public interface NameLike {
    public operator fun plus(other: NameSegments): NameSegments
    public operator fun plus(segment: String): NameSegments = plus(NameSegments(segment))
    public fun resolve(vararg segments: String): NameSegments = plus(NameSegments(*segments))
    public fun asString(): String
}

public data class NameSegments(val segments: List<String>) : NameLike {
    public constructor(vararg segments: String) : this(segments.toList())

    init {
        if (segments.any { '.' in it })
            throw IllegalArgumentException("Segment has illegal character: '.'")
    }

    public override operator fun plus(other: NameSegments): NameSegments = NameSegments(segments + other.segments)
    public override operator fun plus(segment: String): NameSegments = NameSegments(segments + segment)
    public override fun resolve(vararg segments: String): NameSegments = NameSegments(this.segments + segments)

    public override fun asString(): String = segments.joinToString(".")
}

public sealed class Symbol(public val fullName: NameSegments) : NameLike by fullName {

    /**
     * A class, interface, object, etc.
     */
    public data class Classifier(val packageName: NameSegments, val classNames: NameSegments) :
        Symbol(packageName + classNames)

    public data class ClassifierMember(val classifier: Classifier, val name: String) : Symbol(classifier + name) {
    }

    public data class TopLevelMember(val packageName: NameSegments, val name: String) : Symbol(packageName + name)
}