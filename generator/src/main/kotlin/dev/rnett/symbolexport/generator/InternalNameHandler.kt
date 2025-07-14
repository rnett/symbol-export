package dev.rnett.symbolexport.generator

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION

/**
 * Handles InternalName processing and code generation
 */
internal object InternalNameHandler {

    fun generateConstructor(name: InternalName): String = when (name) {
        is InternalName.Classifier -> "Classifier(packageName = ${nameSegmentsOf(name.packageName)}, classNames = ${
            nameSegmentsOf(name.classNames)
        })"

        is InternalName.ClassifierMember -> "ClassifierMember(classifier = ${generateConstructor(name.classifier)}, name = \"${name.name}\")"

        is InternalName.TopLevelMember -> "TopLevelMember(packageName = ${nameSegmentsOf(name.packageName)}, name = \"${name.name}\")"
        is InternalName.EnumEntry -> "EnumEntry(enumClass = ${generateConstructor(name.owner)}, entryName = \"${name.name}\", entryOrdinal = ${name.ordinal})"
        is InternalName.Constructor -> "Constructor(classifier = ${generateConstructor(name.classifier)}, name = \"${name.name}\")"
        is InternalName.TypeParameter -> "TypeParameter(owner=${generateConstructor(name.owner)}, index=${name.index}, name=\"${name.name}\")"
        is InternalName.IndexedParameter -> {
            val ctorName = when (name.type) {
                VALUE -> "ValueParameter"
                CONTEXT -> "ContextParameter"
            }

            val indexParam = when (name.type) {
                VALUE -> "indexInValueParameters"
                CONTEXT -> "indexInContextParameters"
            }

            "$ctorName(owner=${generateConstructor(name.owner)}, index=${name.index}, $indexParam=${name.indexInList}, name=\"${name.name}\")"
        }

        is InternalName.ReceiverParameter -> {
            val ctorName = when (name.type) {
                EXTENSION -> "ExtensionReceiverParameter"
                DISPATCH -> "DispatchReceiverParameter"
            }
            "$ctorName(owner=${generateConstructor(name.owner)}, index=${name.index}, name=\"${name.name}\")"
        }
    }

    fun nameSegmentsOf(segments: List<String>): String =
        "NameSegments(${segments.joinToString(", ") { "\"$it\"" }})"

    fun getAllParts(name: InternalName): List<String> = when (name) {
        is InternalName.Classifier -> name.packageName + name.classNames
        is InternalName.ClassifierMember -> getAllParts(name.classifier) + name.name
        is InternalName.TopLevelMember -> name.packageName + name.name
        is InternalName.EnumEntry -> getAllParts(name.owner) + name.name
        is InternalName.IndexedParameter -> getAllParts(name.owner) + name.name
        is InternalName.Constructor -> getAllParts(name.classifier) + name.name
        is InternalName.ReceiverParameter -> getAllParts(name.owner) + name.name
        is InternalName.TypeParameter -> getAllParts(name.owner) + name.name
    }

    fun getType(name: InternalName): String = when (name) {
        is InternalName.Classifier -> "Classifier"
        is InternalName.ClassifierMember -> "ClassifierMember"
        is InternalName.TopLevelMember -> "TopLevelMember"
        is InternalName.EnumEntry -> "EnumEntry"
        is InternalName.Constructor -> "Constructor"
        is InternalName.TypeParameter -> "TypeParameter"
        is InternalName.IndexedParameter -> when (name.type) {
            VALUE -> "ValueParameter"
            CONTEXT -> "ContextParameter"
        }

        is InternalName.ReceiverParameter -> when (name.type) {
            EXTENSION -> "ExtensionReceiverParameter"
            DISPATCH -> "DispatchReceiverParameter"
        }
    }

    fun getFieldName(name: InternalName): String =
        getAllParts(name).joinToString("_") { it.replace("<", "").replace(">", "") }
}