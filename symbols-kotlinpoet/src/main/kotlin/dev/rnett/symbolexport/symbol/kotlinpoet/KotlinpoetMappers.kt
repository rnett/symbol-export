package dev.rnett.symbolexport.symbol.kotlinpoet

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import dev.rnett.symbolexport.symbol.Symbol

public fun Symbol.Classifier.asClassName(): ClassName = ClassName(packageName.asString(), classNames.segments)

public fun Symbol.Member.asMemberName(isExtension: Boolean = false): MemberName = when (this) {
    is Symbol.ClassifierMember -> MemberName(classifier.asClassName(), name, isExtension)
    is Symbol.TopLevelMember -> MemberName(packageName.asString(), name, isExtension)
}