package com.rnett.symbolexport

import com.rnett.symbolexport.internal.InternalName


fun interface NameReporter {
    fun reportName(name: InternalName)
}