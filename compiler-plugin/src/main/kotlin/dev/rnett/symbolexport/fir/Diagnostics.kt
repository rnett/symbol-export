package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactory1
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.warning0
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.renderReadable
import org.jetbrains.kotlin.psi.KtDeclaration

object Diagnostics : KtDiagnosticsContainer() {
    val SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED by error0<KtDeclaration>()
    val SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS by error0<KtDeclaration>()
    val SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API by error1<KtDeclaration, String>()
    val SYMBOL_EXPORT_NO_CONTEXT_RECEIVER by error0<KtDeclaration>()
    val SYMBOL_EXPORT_EXPORTING_FROM_EXPORTED_ANNOTATION by warning0<KtDeclaration>()
    val SYMBOL_EXPORT_EXPORTING_EXPORTED_ANNOTATION by warning0<KtDeclaration>()
    val SYMBOL_EXPORT_EXPORTED_ANNOTATION_NOT_AN_ANNOTATION_CLASS by error0<KtDeclaration>()
    val SYMBOL_EXPORT_EXPORTED_ANNOTATION_ANNOTATION_PROPERTY_NOT_EXPORTED by error0<KtDeclaration>()
    val SYMBOL_EXPORT_UNSUPPORTED_ANNOTATION_PROPERTY_TYPE by error1<KtDeclaration, ConeKotlinType>()

    fun symbolExportMarker(name: InternalName): KtDiagnosticFactory1<InternalName> {
        return KtDiagnosticFactory1(
            "EXPORTED_MARKER_$name",
            Severity.WARNING,
            SourceElementPositioningStrategies.DEFAULT,
            KtDeclaration::class,
            RenderFactory,
        )
    }

    object RenderFactory : BaseDiagnosticRendererFactory() {
        override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap("SymbolExport") {
            it.put(
                SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED,
                "Parents of '@ExportSymbol' declarations must also be annotated with '@ExportSymbol' or '@ChildrenExported'."
            )
            it.put(
                SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS,
                "'@ExportSymbol' on local declarations is not supported."
            )
            it.put(
                SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API,
                "'@ExportSymbol' declarations must be 'public' or '@PublishedApi internal', but was {0}.",
                Renderer {
                    it
                }
            )
            it.put(
                SYMBOL_EXPORT_NO_CONTEXT_RECEIVER,
                "Can not export context receivers, use context parameters instead."
            )
            it.put(
                SYMBOL_EXPORT_EXPORTING_FROM_EXPORTED_ANNOTATION,
                "Exporting parameters of exported annotations is redundant."
            )
            it.put(
                SYMBOL_EXPORT_EXPORTING_EXPORTED_ANNOTATION,
                "@ExportAnnotation implies @ExportSymbol, there is no need to also use it."
            )
            it.put(
                SYMBOL_EXPORT_EXPORTED_ANNOTATION_NOT_AN_ANNOTATION_CLASS,
                "@ExportAnnotation can only be applied to annotation classes."
            )
            it.put(
                SYMBOL_EXPORT_EXPORTED_ANNOTATION_ANNOTATION_PROPERTY_NOT_EXPORTED,
                "Annotation properties of @ExportAnnotation annotations must also be marked with @ExportAnnotation."
            )
            it.put(
                SYMBOL_EXPORT_UNSUPPORTED_ANNOTATION_PROPERTY_TYPE,
                "Could not create annotation type representation for {0}. This is a bug, please report it..",
                Renderer {
                    it.renderReadable()
                }
            )
        }
    }

    override fun getRendererFactory() = RenderFactory
}