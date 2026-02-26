package dev.rnett.symbolexport.ir

import dev.rnett.kcp.development.utils.ir.ExperimentalIrHelpers
import dev.rnett.kcp.development.utils.ir.IrFullProcessor
import dev.rnett.symbolexport.Names
import dev.rnett.symbolexport.internal.InternalDeclaration
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

@OptIn(ExperimentalIrHelpers::class)
class IrDeclarationExporter(cntext: IrPluginContext) : IrFullProcessor(cntext) {
    val declarations = FileMap<InternalDeclaration>()
    val declarationFactory = DeclarationFactory(cntext)

    override fun visitClass(declaration: IrClass) {
        if (declaration.isPublicAbi() && declaration.hasAnnotation(Names.ExportDeclaration)) {
            declarations[declaration] = declarationFactory.classDeclaration(declaration)
        }
        super.visitClass(declaration)
    }

    override fun visitFunction(declaration: IrFunction) {
        //TODO signature is always null!?
        if (declaration.isPublicAbi() && declaration.hasAnnotation(Names.ExportDeclaration)) {
            declarations[declaration] = declarationFactory.functionDeclaration(declaration)
        }
        super.visitFunction(declaration)
    }

    override fun visitProperty(declaration: IrProperty) {
        if (declaration.isPublicAbi() && declaration.hasAnnotation(Names.ExportDeclaration)) {
            declarations[declaration] = declarationFactory.propertyDeclaration(declaration)
        }
        super.visitProperty(declaration)
    }

    private fun shouldContinue(declaration: IrDeclaration): Boolean {
        if (!declaration.isPublicAbi()) return false
        return declaration.hasAnnotation(Names.ExportSymbol) ||
                declaration.hasAnnotation(Names.ExportDeclaration) ||
                declaration.hasAnnotation(Names.ChildrenExported)
    }

    override fun visitElement(element: IrElement) {
        if (element is IrDeclaration) {
            if (shouldContinue(element)) {
                element.acceptChildrenVoid(this)
            }
        } else if (element is IrDeclarationParent) {
            element.acceptChildrenVoid(this)
        }
    }
}