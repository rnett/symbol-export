package dev.rnett.symbolexport

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Names {
    val EXPORT_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportSymbol")
    val ExportSymbol = ClassId.Companion.topLevel(EXPORT_ANNOTATION_FQN)

    val PARENT_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ChildrenExported")
    val ChildrenExported = ClassId.Companion.topLevel(PARENT_ANNOTATION_FQN)

    val EXPORT_RECEIVERS_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportReceivers")
    val ExportReceivers = ClassId.Companion.topLevel(EXPORT_RECEIVERS_ANNOTATION_FQN)


    val EXPORT_RECEIVERS_DISPATCH_PROP = Name.identifier("dispatch")
    val EXPORT_RECEIVERS_EXTENSION_PROP = Name.identifier("extension")

    val EXPORT_ANNOTATION_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportAnnotation")
    val ExportAnnotation = ClassId.topLevel(EXPORT_ANNOTATION_ANNOTATION_FQN)

    val EXPORT_REFERENCES_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.reference.ExportReferences")
    val EXPORT_REFERENCES_ANNOTATION_CLASSID = ClassId.Companion.topLevel(EXPORT_REFERENCES_ANNOTATION_FQN)

    val ExportParameters = ClassId.topLevel(FqName("dev.rnett.symbolexport.ExportParameters"))
    val ExportDeclaration = ClassId.topLevel(FqName("dev.rnett.symbolexport.ExportDeclaration"))
}