package test.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import test.cases.Cases
import test.cases.IrContext

class IrRegistrar : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.acceptVoid(Visitor(pluginContext))
    }
}

private class Visitor(val context: IrPluginContext) : IrVisitorVoid() {
    val ctx = IrContext(context)
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitClass(declaration: IrClass) {
        Cases.casesFor(declaration.name.asString()).forEach {
            it.ir.apply { ctx.test(declaration) }
        }
        super.visitClass(declaration)
    }
}