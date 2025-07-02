package dev.rnett.symbolexport.symbol

public interface NameLike {
    public val segments: List<String>
    public fun asString(): String = segments.joinToString(".")

    public operator fun plus(other: NameSegments): NameSegments
    public operator fun plus(segment: String): NameSegments = plus(NameSegments(segment))
    public fun resolve(vararg segments: String): NameSegments = plus(NameSegments(*segments))
}

public data class NameSegments(override val segments: List<String>) : NameLike {
    public constructor(vararg segments: String) : this(segments.toList())

    init {
        if (segments.any { '.' in it })
            throw IllegalArgumentException("Segment has illegal character: '.'")
    }

    public override operator fun plus(other: NameSegments): NameSegments = NameSegments(segments + other.segments)
    public override operator fun plus(segment: String): NameSegments = NameSegments(segments + segment)
    public override fun resolve(vararg segments: String): NameSegments = NameSegments(this.segments + segments)
}

public sealed class Symbol(public val fullName: NameSegments) : NameLike by fullName {

    /**
     * A class, interface, object, etc.
     */
    public data class Classifier(val packageName: NameSegments, val classNames: NameSegments) :
        Symbol(packageName + classNames)

    public sealed class Member(fullName: NameSegments) : Symbol(fullName) {
        public abstract val name: String
    }

    public data class ClassifierMember(val classifier: Classifier, override val name: String) :
        Member(classifier + name) {
    }

    public data class TopLevelMember(val packageName: NameSegments, override val name: String) :
        Member(packageName + name)
}