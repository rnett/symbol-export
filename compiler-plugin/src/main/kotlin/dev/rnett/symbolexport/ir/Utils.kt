package dev.rnett.symbolexport.ir

import dev.rnett.symbolexport.Names
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithVisibility
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isPublishedApi
import org.jetbrains.kotlin.name.ClassId
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo


fun IrDeclaration.parentHasAnnotation(annotation: ClassId) = parent.let { it is IrAnnotationContainer && it.hasAnnotation(annotation) }

fun IrDeclaration.findExportedName(): String? {
    if (this !is IrFunction) return null

    val annotation = annotations.findAnnotation(Names.ExportSymbol.asSingleFqName()) ?: annotations.findAnnotation(Names.ExportDeclaration.asSingleFqName()) ?: return null
    val arg = annotation.arguments[0] as? IrConst ?: return null

    return arg.value as? String
}

fun IrDeclaration.isPublicAbi() = if (this is IrDeclarationWithVisibility)
    this.visibility.isPublicAPI || (this.visibility == DescriptorVisibilities.INTERNAL && this.isPublishedApi())
else
    this.isPublishedApi()

class FileMap<T> {
    private val raw = mutableMapOf<Path, MutableSet<T>>()

    operator fun set(declaration: IrDeclaration, internalDeclaration: T) {
        raw.getOrPut(Path(declaration.file.fileEntry.name)) { mutableSetOf() } += internalDeclaration
    }

    fun asMap(root: Path?) = raw.mapValues { it.value.toSet() }.mapKeys {
        if (root != null)
            it.key.relativeTo(root).pathString
        else
            it.key.absolutePathString()
    }
}