package dev.rnett.symbolexport.analyzer

import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import dev.rnett.symbolexport.internal.InternalNameEntry
import dev.rnett.symbolexport.internal.ParameterType
import dev.rnett.symbolexport.internal.ProjectCoordinates
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.analysis.api.KaContextParameterApi
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.KaImplementationDetail
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.components.combinedDeclaredMemberScope
import org.jetbrains.kotlin.analysis.api.components.containingDeclaration
import org.jetbrains.kotlin.analysis.api.components.fileScope
import org.jetbrains.kotlin.analysis.api.components.isPublicApi
import org.jetbrains.kotlin.analysis.api.components.packageScope
import org.jetbrains.kotlin.analysis.api.projectStructure.KaModule
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.analysis.api.symbols.KaCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaConstructorSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaContextParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaDeclarationSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaEnumEntrySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFileSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaKotlinPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPackageSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaTypeParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.contextParameters
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaAnnotatedSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KaDeclarationContainerSymbol
import org.jetbrains.kotlin.analysis.api.symbols.symbol
import org.jetbrains.kotlin.analysis.api.symbols.typeParameters
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.KtFile
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText

@OptIn(KaExperimentalApi::class, KaContextParameterApi::class)
class SymbolExportAnalyzer(
    val projectName: String,
    val projectCoordinates: ProjectCoordinates,
    val sourceSetName: String,
    val outputDir: Path
) : Analyzer {

    context(session: KaSession)
    override fun analyze(module: KaModule) {
        if (module !is KaSourceModule) return
        val exportedDeclarations = module.psiRoots
            .filterIsInstance<KtFile>()
            .flatMap { ktFile ->
                allExportedSymbols(ktFile.symbol)
            }
        val names = exportedDeclarations.flatMap { export(it) }
            .map {
                InternalNameEntry(
                    projectName,
                    projectCoordinates,
                    sourceSetName,
                    it
                )
            }
            .toList()

        if (names.isNotEmpty()) {
            val json = Json { prettyPrint = true }
            val outputFile = outputDir.resolve("symbols.json")
            outputFile.createParentDirectories()
            outputFile.writeText(json.encodeToString(names))
        }
    }

    context(session: KaSession)
    private fun canChildrenBeExported(declaration: KaAnnotatedSymbol): Boolean {
        //TODO do I want/need this?
        return Names.Annotations.ChildrenExported in declaration.annotations ||
                (declaration is KaDeclarationSymbol && isExported(declaration))
    }

    context(session: KaSession)
    private fun isExported(declaration: KaAnnotatedSymbol): Boolean {
        return Names.Annotations.ExportSymbol in declaration.annotations
    }

    context(session: KaSession)
    private fun isExported(declaration: KaDispatchReceiverSymbol): Boolean {
        //TODO actual logic
        return false
    }

    @OptIn(KaImplementationDetail::class)
    class KaDispatchReceiverSymbol(val parent: KaCallableSymbol, val cls: KaNamedClassSymbol) : KaSymbol by parent

    context(session: KaSession)
    private fun KaSymbol.exportableChildren(): Sequence<KaSymbol> {
        return when (val symbol = this) {
            is KaFileSymbol -> fileScope.declarations
            is KaPackageSymbol -> packageScope.declarations
            else -> sequence {
                if (symbol is KaDeclarationContainerSymbol)
                    yieldAll(combinedDeclaredMemberScope.declarations)
                if (symbol is KaDeclarationSymbol)
                    yieldAll(typeParameters)

                if (symbol is KaCallableSymbol) {
                    yieldAll(contextParameters)
                    receiverParameter?.let { yield(it) }
                }
                if (symbol is KaFunctionSymbol) {
                    yieldAll(valueParameters)
                }
                if (symbol is KaNamedFunctionSymbol) {
                    if (!symbol.isStatic) {
                        val cls = symbol.containingDeclaration as? KaNamedClassSymbol
                        if (cls != null) {
                            yield(KaDispatchReceiverSymbol(symbol, cls))
                        }
                    }
                }
                if (symbol is KaPropertySymbol) {
                    if (!symbol.isStatic) {
                        val cls = symbol.containingDeclaration as? KaNamedClassSymbol
                        if (cls != null) {
                            yield(KaDispatchReceiverSymbol(symbol, cls))
                        }
                    }
                }
            }
        }
    }

    context(session: KaSession)
    private fun allExportedSymbols(declaration: KaSymbol): Sequence<KaSymbol> {
        if (declaration is KaFileSymbol) {
            return declaration.exportableChildren().flatMap { allExportedSymbols(it) }
        } else if (declaration is KaDeclarationSymbol) {

            if (!isPublicApi(declaration)) return sequenceOf()

            val isExported = isExported(declaration)
            val isChildrenExported = canChildrenBeExported(declaration)



            if (!isExported && !isChildrenExported) return sequenceOf()
            if (!isChildrenExported) return sequenceOf(declaration)
            if (!isExported) return declaration.exportableChildren().flatMap { allExportedSymbols(it) }
            return sequence {
                yield(declaration)
                yieldAll(declaration.exportableChildren().flatMap { allExportedSymbols(it) })
            }
        } else if (declaration is KaDispatchReceiverSymbol) {
            if (isExported(declaration)) return sequenceOf(declaration)
        }
        return emptySequence()
    }

    context(session: KaSession)
    private fun export(symbol: KaSymbol): Sequence<InternalName> {
        return when (symbol) {
            is KaDispatchReceiverSymbol -> sequenceOf(exportDispatchReceiver(symbol))

            !is KaDeclarationSymbol -> emptySequence()
            else if !isPublicApi(symbol) -> emptySequence()

            else if !isExported(symbol) -> sequenceOf()
            is KaConstructorSymbol,
            is KaNamedFunctionSymbol,
            is KaKotlinPropertySymbol -> sequenceOf(exportMember(symbol))

            is KaEnumEntrySymbol -> sequenceOf(exportEnumEntry(symbol))
            is KaContextParameterSymbol,
            is KaReceiverParameterSymbol,
            is KaValueParameterSymbol -> sequenceOf(exportParameter(symbol))

            is KaNamedClassSymbol -> sequenceOf(exportClass(symbol))
            is KaTypeParameterSymbol -> sequenceOf(exportTypeParameter(symbol))

            else -> sequenceOf() // not-applicable
        }
    }

    context(session: KaSession)
    private fun className(classSymbol: KaNamedClassSymbol): InternalName.Classifier {
        val classId = classSymbol.classId ?: error("Class symbol $classSymbol has no classId")
        return InternalName.Classifier(
            classId.packageFqName.pathSegments().map { it.asString() },
            classId.relativeClassName.pathSegments().map { it.asString() }
        )
    }

    context(session: KaSession)
    private fun exportMember(symbol: KaCallableSymbol): InternalName.Member {
        val callableId = symbol.callableId ?: error("Callable symbol $symbol has no callableId")

        if (symbol is KaConstructorSymbol) {
            val classId = callableId.classId ?: error("Constructor $symbol has no classId")
            return InternalName.Constructor(
                InternalName.Classifier(
                    classId.packageFqName.pathSegments().map { it.asString() },
                    classId.relativeClassName.pathSegments().map { it.asString() }
                ),
                SpecialNames.INIT.asString()
            )
        }

        val packageName = callableId.packageName.pathSegments().map { it.asString() }
        val relativeClassName = callableId.className?.pathSegments()?.map { it.asString() }

        return if (relativeClassName == null) {
            InternalName.TopLevelMember(packageName, callableId.callableName.asString())
        } else {
            InternalName.ClassifierMember(
                InternalName.Classifier(packageName, relativeClassName),
                callableId.callableName.asString()
            )
        }
    }

    context(session: KaSession)
    private fun getIndexStartOffset(symbol: KaCallableSymbol, type: ParameterType): Int {
        return when (type) {
            DISPATCH -> 0
            CONTEXT -> if (symbol.isStatic) 1 else 0
            EXTENSION -> getIndexStartOffset(symbol, CONTEXT) + symbol.contextParameters.size
            VALUE -> getIndexStartOffset(symbol, EXTENSION) + if (symbol.receiverParameter != null) 1 else 0
        }
    }

    context(session: KaSession)
    private fun exportClass(symbol: KaNamedClassSymbol): InternalName {
        return className(symbol)
    }

    context(session: KaSession)
    private fun exportEnumEntry(symbol: KaEnumEntrySymbol): InternalName {
        val parent = symbol.containingDeclaration as? KaNamedClassSymbol ?: error("Enum entry $symbol has no parent class")
        val ordinal = parent.combinedDeclaredMemberScope.declarations
            .filterIsInstance<KaEnumEntrySymbol>()
            .toList()
            .indexOf(symbol)

        if (ordinal < 0) error("Could not find enum entry $symbol in $parent")

        return InternalName.EnumEntry(className(parent), symbol.name.asString(), ordinal)
    }

    context(session: KaSession)
    private fun exportTypeParameter(symbol: KaTypeParameterSymbol): InternalName {
        val parent = symbol.containingDeclaration
        val parentName = when (parent) {
            is KaCallableSymbol -> exportMember(parent)
            is KaNamedClassSymbol -> className(parent)
            else -> error("Unknown parent type $parent for type parameter $symbol")
        }

        val indexInList = parent.typeParameters.indexOf(symbol)

        if (indexInList < 0) error("Could not find type parameter $symbol in $parent")

        return InternalName.TypeParameter(parentName, symbol.name.asString(), indexInList)
    }

    context(session: KaSession)
    private fun exportParameter(symbol: KaParameterSymbol): InternalName {
        val parent = symbol.containingDeclaration as? KaCallableSymbol ?: error("Parameter $symbol has no callable parent")
        val parentName = exportMember(parent)

        return when (symbol) {
            is KaReceiverParameterSymbol -> {
                val type = EXTENSION
                InternalName.ReceiverParameter(
                    parentName,
                    SpecialNames.RECEIVER.asString(),
                    getIndexStartOffset(parent, type),
                    type
                )
            }

            is KaValueParameterSymbol -> {
                val type = VALUE
                val indexInList = (parent as KaFunctionSymbol).valueParameters.indexOf(symbol)
                InternalName.IndexedParameter(
                    parentName,
                    symbol.name.asString(),
                    getIndexStartOffset(parent, type) + indexInList,
                    indexInList,
                    type
                )
            }

            is KaContextParameterSymbol -> {
                val type = CONTEXT
                val contextParameters = when (parent) {
                    is KaFunctionSymbol -> parent.contextParameters
                    is KaKotlinPropertySymbol -> parent.contextParameters
                    else -> error("Unknown callable parent type $parent for context parameter $symbol")
                }
                val indexInList = contextParameters.indexOf(symbol)
                InternalName.IndexedParameter(
                    parentName,
                    symbol.name.asString(),
                    getIndexStartOffset(parent, type) + indexInList,
                    indexInList,
                    type
                )
            }
        }
    }

    context(session: KaSession)
    private fun exportDispatchReceiver(symbol: KaDispatchReceiverSymbol): InternalName {
        return InternalName.ReceiverParameter(
            exportMember(symbol.parent),
            SpecialNames.THIS.asString(),
            0,
            DISPATCH
        )
    }
}

val KaCallableSymbol.isStatic: Boolean
    get() = when (this) {
        is KaNamedFunctionSymbol -> this.isStatic
        is KaPropertySymbol -> this.isStatic
        else -> false
    }