package dev.rnett.symbolexport.symbol.v2

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toPersistentList
import kotlin.jvm.JvmInline

public sealed interface QualifiedName {
    public val segments: List<String>
    public val name: String

    @JvmInline
    public value class PackageName private constructor(internal val _segments: PersistentList<String>) : QualifiedName {
        override val segments: List<String> get() = _segments

        public constructor(segments: List<String>) : this(segments.toPersistentList())

        override val name: String
            get() = segments.lastOrNull() ?: ""
        public val isRoot: Boolean get() = segments.isEmpty()

        public fun child(name: String): PackageName = QualifiedName.PackageName(_segments + name)
        public fun className(name: String): ClassName = ClassName(this, persistentListOf(name))
        public fun callableName(name: String): CallableName = TopLevelCallableName(this, name)

        override fun toString(): String = _segments.joinToString(".")
    }

    @ConsistentCopyVisibility
    public data class ClassName private constructor(val packageName: PackageName, internal val _classNames: PersistentList<String>) : QualifiedName {
        public constructor(packageName: PackageName, classNames: List<String>) : this(packageName, classNames.toPersistentList())

        val classNames: List<String> get() = _classNames

        init {
            require(_classNames.isNotEmpty()) { "ClassName must have at least 1 class name" }
        }

        //TODO ensure that this doesn't allocate array
        override val segments: List<String> = packageName._segments + classNames
        override val name: String
            get() = _classNames.last()

        public fun childClass(name: String): ClassName = ClassName(packageName, _classNames + name)

        override fun toString(): String = buildString {
            if (!packageName.isRoot) {
                append(packageName)
                append(".")
            }
            append(_classNames.joinToString("."))
        }
    }

    public sealed interface CallableName : QualifiedName {
        public override val name: String
    }

    public data class MemberName(val className: ClassName, override val name: String) : CallableName {
        override val segments: List<String> = className.packageName._segments + className._classNames + name
        override fun toString(): String = "$className.$name"
    }

    public data class TopLevelCallableName(val packageName: PackageName, override val name: String) : CallableName {
        override val segments: List<String> = packageName._segments + name
        override fun toString(): String = buildString {
            if (!packageName.isRoot) {
                append(packageName)
                append(".")
            }
            append(name)
        }
    }


}