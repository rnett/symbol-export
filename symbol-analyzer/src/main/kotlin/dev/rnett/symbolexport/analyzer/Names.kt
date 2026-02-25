package dev.rnett.symbolexport.analyzer

import org.jetbrains.kotlin.name.ClassId

object Names {
    object Annotations {
        val ExportSymbol = ClassId.fromString("dev/rnett/symbolexport/ExportSymbol")
        val ChildrenExported = ClassId.fromString("dev/rnett/symbolexport/ChildrenExported")
    }
}