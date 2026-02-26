package dev.rnett.symbolexport.ir

import dev.rnett.symbolexport.PluginComponentRegistrar
import dev.rnett.symbolexport.internal.InternalNameSerializer
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText

class IrExtension(val spec: PluginComponentRegistrar.Spec) : IrGenerationExtension {
    @OptIn(ExperimentalPathApi::class)
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {

        val outputDir = spec.outputDir ?: return

        val symbolExporter = IrSymbolExporter(pluginContext)
        val declarationExporter = IrDeclarationExporter(pluginContext)

        moduleFragment.acceptChildrenVoid(symbolExporter)
        moduleFragment.acceptChildrenVoid(declarationExporter)

        outputDir.deleteRecursively()
        outputDir.createDirectories()
        outputDir.resolve("symbols.json").writeText(InternalNameSerializer.serialize(symbolExporter.symbols.asMap(spec.rootPath).values.flatten()))
        outputDir.resolve("declarations.json").writeText(InternalNameSerializer.serialize(declarationExporter.declarations.asMap(spec.rootPath).values.flatten()))
        outputDir.resolve("targets.json")
            .writeText(InternalNameSerializer.serialize(moduleFragment.descriptor.platform?.map { it.platformName }?.toSet().orEmpty()))
    }
}