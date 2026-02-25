package dev.rnett.symbolexport.fir

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object Names {
    val EXPORT_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportSymbol")
    val EXPORT_ANNOTATION_CLASSID = ClassId.topLevel(EXPORT_ANNOTATION_FQN)

    val PARENT_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ChildrenExported")
    val PARENT_ANNOTATION_CLASSID = ClassId.topLevel(PARENT_ANNOTATION_FQN)

    val EXPORT_RECEIVERS_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportReceivers")
    val EXPORT_RECEIVERS_ANNOTATION_CLASSID = ClassId.topLevel(EXPORT_RECEIVERS_ANNOTATION_FQN)


    val EXPORT_RECEIVERS_DISPATCH_PROP = Name.identifier("dispatch")
    val EXPORT_RECEIVERS_EXTENSION_PROP = Name.identifier("extension")

    val EXPORT_ANNOTATION_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportAnnotation")
    val EXPORT_ANNOTATION_ANNOTATION_CLASSID = ClassId.topLevel(EXPORT_ANNOTATION_ANNOTATION_FQN)

    val EXPORT_REFERENCES_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.reference.ExportReferences")
    val EXPORT_REFERENCES_ANNOTATION_CLASSID = ClassId.topLevel(EXPORT_REFERENCES_ANNOTATION_FQN)

    val ExportParameters = ClassId.topLevel(FqName("dev.rnett.symbolexport.ExportParameters"))
}