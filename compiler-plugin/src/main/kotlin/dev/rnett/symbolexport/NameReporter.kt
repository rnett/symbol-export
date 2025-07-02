package dev.rnett.symbolexport

import dev.rnett.symbolexport.internal.InternalName


fun interface NameReporter {
    fun reportName(name: InternalName)
}