package dev.rnett.symbolexport.analyzer

import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule

fun interface Analyzer {
    context(session: KaSession)
    fun analyze(module: KaModule)

    fun doAnalysis(module: KaModule) {
        org.jetbrains.kotlin.analysis.api.analyze(module) {
            analyze(module)
        }
    }
}