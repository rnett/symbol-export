package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtDeclaration

object Errors : BaseDiagnosticRendererFactory() {
    val SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED by error0<KtDeclaration>()
    val SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS by error0<KtDeclaration>()
    val SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API by error1<KtDeclaration, String>()
    val SYMBOL_EXPORT_NO_CONTEXT_RECEIVER by error0<KtDeclaration>()

    fun symbolExportMarker(name: InternalName): KtDiagnosticFactory1<InternalName> {
        return KtDiagnosticFactory1(
            "EXPORTED_MARKER_$name",
            Severity.WARNING,
            SourceElementPositioningStrategies.DEFAULT,
            KtDeclaration::class
        )
    }

    override val MAP = KtDiagnosticFactoryToRendererMap("SymbolExport").apply {
        put(
            SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED,
            "Parents of '@ExportSymbol' declarations must also be annotated with '@ExportSymbol' or '@ChildrenExported'."
        )
        put(
            SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS,
            "'@ExportSymbol' on local declarations is not supported."
        )
        put(
            SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API,
            "'@ExportSymbol' declarations must be 'public' or '@PublishedApi internal', but was {0}.",
            Renderer {
                it
            }
        )
        put(
            SYMBOL_EXPORT_NO_CONTEXT_RECEIVER,
            "Can not export context receivers, use context parameters instead."
        )
    }

    init {
        RootDiagnosticRendererFactory.registerFactory(this)
    }
}