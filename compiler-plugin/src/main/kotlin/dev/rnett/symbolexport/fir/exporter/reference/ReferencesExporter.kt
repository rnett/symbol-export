package dev.rnett.symbolexport.fir.exporter.reference

import dev.rnett.symbolexport.fir.Predicates
import dev.rnett.symbolexport.fir.exporter.InternalNames
import dev.rnett.symbolexport.fir.exporter.SymbolExporter
import dev.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1DelegateProvider
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.diagnostics.warning0
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.extractClassFromArgument
import org.jetbrains.kotlin.fir.analysis.checkers.findNonInterfaceSupertype
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.checkers.toRegularClassSymbol
import org.jetbrains.kotlin.fir.declarations.DirectDeclarationsAccess
import org.jetbrains.kotlin.fir.declarations.FirAnonymousInitializer
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.collectEnumEntries
import org.jetbrains.kotlin.fir.declarations.evaluateAs
import org.jetbrains.kotlin.fir.declarations.processAllDeclarations
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.declarations.utils.isNonLocal
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirLiteralExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.resolvedArgumentMapping
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousInitializerSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFieldSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression

class ReferencesExporter(val session: FirSession) : SymbolExporter<FirClass> {

    object ExportFunctionNames {
        val baseClass = ClassId(FqName("dev.rnett.symbolexport.reference"), Name.identifier("BaseReferenceExporter"))

        val placeholder = CallableId(baseClass, Name.identifier("placeholder"))

        val exportReferenced = CallableId(baseClass, Name.identifier("exportReferenced"))
        val exportReference = CallableId(baseClass, Name.identifier("exportReference"))
        val exportClass = CallableId(baseClass, Name.identifier("exportClass"))
        val exportAnnotation = CallableId(baseClass, Name.identifier("exportAnnotation"))
        val exportEnumEntries = CallableId(baseClass, Name.identifier("exportEnumEntries"))

        val exportFunctions = setOf(exportReferenced, exportReference, exportClass, exportAnnotation, exportEnumEntries)

        val includeParameters = Name.identifier("includeParameters")
        val includeTypeParameters = Name.identifier("includeTypeParameters")
    }


    @OptIn(DirectDeclarationsAccess::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun exportSymbols(declaration: FirClass): Iterable<Pair<KtSourceElement?, InternalName>> {
        val extendsBaseClass = declaration.findNonInterfaceSupertype(context)?.toRegularClassSymbol(session)?.classId == ExportFunctionNames.baseClass
        val annotated = session.predicateBasedProvider.matches(Predicates.exportReferences, declaration)
        if (extendsBaseClass && !annotated) {
            reporter.reportOn(declaration.source, Errors.SYMBOL_EXPORT_REFERENCE_MUST_BE_ANNOTATED)
            return emptyList()
        }

        if (annotated && !extendsBaseClass) {
            reporter.reportOn(declaration.source, Errors.SYMBOL_EXPORT_REFERENCE_MUST_EXTEND_BASE)
        }

        if (!annotated) {
            return emptyList()
        }

        if (declaration.classKind != ClassKind.OBJECT) {
            reporter.reportOn(declaration.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_OBJECT)
            return emptyList()
        }

        if (declaration.isLocal) {
            reporter.reportOn(declaration.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_OBJECT)
            return emptyList()
        }

        val initBlocks = mutableSetOf<FirAnonymousInitializerSymbol>()
        declaration.declarations.filterIsInstance<FirAnonymousInitializer>().mapTo(initBlocks) { it.symbol }

        declaration.processAllDeclarations(session) {
            if (it is FirAnonymousInitializerSymbol)
                initBlocks += it
            else {
                if (it is FirConstructorSymbol && it.isPrimary)
                    return@processAllDeclarations

                reporter.reportOn(it.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_INIT_BLOCK)
            }
        }

        return initBlocks.flatMap {
            processInitBlock(it)
        }
    }

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun processInitBlock(initBlock: FirAnonymousInitializerSymbol): List<Pair<KtSourceElement?, InternalName>> {
        val block = initBlock.fir
        val statements = block.body?.statements?.ifEmpty { null } ?: return emptyList()

        return statements.flatMap {
            processInitStatement(it)
        }
    }


    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun processInitStatement(statement: FirStatement): List<Pair<KtSourceElement?, InternalName>> {
        statement.acceptChildren(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {
                element.acceptChildren(this)
            }

            override fun visitFunctionCall(functionCall: FirFunctionCall) {
                if (functionCall.calleeReference.toResolvedCallableSymbol()?.callableId in ExportFunctionNames.exportFunctions) {
                    reporter.reportOn(functionCall.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_IN_INIT_BLOCK)
                }
                super.visitFunctionCall(functionCall)
            }

        })

        if (statement is FirFunctionCall) {
            val target = statement.calleeReference.toResolvedCallableSymbol() ?: return emptyList()

            val exported = when (target.callableId) {
                ExportFunctionNames.exportReferenced -> processExportReferenced(statement).map { statement.source to it }
                ExportFunctionNames.exportReference -> processExportReference(statement).map { statement.source to it }
                ExportFunctionNames.exportClass -> processExportClass(statement).map { statement.source to it }
                ExportFunctionNames.exportAnnotation -> processExportAnnotation(statement).map { statement.source to it }
                ExportFunctionNames.exportEnumEntries -> processExportEnumEntries(statement).map { statement.source to it }
                else -> {
                    reporter.reportOn(statement.source, Errors.SYMBOL_EXPORT_REFERENCE_MEANINGLESS_STATEMENT)
                    null
                }
            }

            if (!exported.isNullOrEmpty()) {
                reporter.reportOn(statement.source, Errors.SYMBOL_EXPORT_REFERENCE_EXPORTING, exported.map { it.second })
            }

            return exported ?: emptyList()

        }
        return emptyList()
    }


    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun checkExportedSymbol(source: KtSourceElement?, symbol: FirBasedSymbol<*>): Boolean {

        if (session.predicateBasedProvider.matches(Predicates.ancestorExportsReferences, symbol)) {
            reporter.reportOn(source, Errors.SYMBOL_EXPORT_REFERENCE_REFERENCE_DECLARATION)
            return false
        }

        if (symbol.getContainingClassSymbol()?.classId == ExportFunctionNames.baseClass) {
            reporter.reportOn(source, Errors.SYMBOL_EXPORT_REFERENCE_REFERENCE_DECLARATION)
            return false
        }

        if (!symbol.fir.isNonLocal) {
            reporter.reportOn(source, Errors.SYMBOL_EXPORT_REFERENCE_LOCAL_DECLARATION)
            return false
        }
        return true
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun processExportReferenced(call: FirFunctionCall): List<InternalName> {
        val includeParameters = call.getBooleanArgument(ExportFunctionNames.includeParameters, false) ?: return emptyList()
        val includeTypeParameters = call.getBooleanArgument(ExportFunctionNames.includeTypeParameters, false) ?: return emptyList()

        val referenced = call.arguments[0]
        val referencedSymbol = referenced.toResolvedCallableSymbol(session)

        if (referencedSymbol == null) {
            reporter.reportOn(referenced.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_ACCESS_LITERAL)
            return emptyList()
        }

        return processCallableSymbol(referenced.source, referencedSymbol, includeParameters, includeTypeParameters)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun processExportReference(call: FirFunctionCall): List<InternalName> {
        val includeParameters = call.getBooleanArgument(ExportFunctionNames.includeParameters, false) ?: return emptyList()
        val includeTypeParameters = call.getBooleanArgument(ExportFunctionNames.includeTypeParameters, false) ?: return emptyList()

        val referenced = call.arguments[0]
        val referencedSymbol = (referenced as? FirCallableReferenceAccess)?.calleeReference?.toResolvedCallableSymbol()

        if (referencedSymbol == null) {
            reporter.reportOn(referenced.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_REFERENCE_LITERAL)
            return emptyList()
        }

        return processCallableSymbol(referenced.source, referencedSymbol, includeParameters, includeTypeParameters)
    }

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)

    private fun processCallableSymbol(source: KtSourceElement?, symbol: FirCallableSymbol<*>, includeParameters: Boolean, includeTypeParameters: Boolean): List<InternalName> {
        if (!checkExportedSymbol(source, symbol)) return emptyList()

        if (symbol is FirFunctionSymbol<*> || symbol is FirPropertySymbol || symbol is FirFieldSymbol) {
            return buildList {
                add(InternalNames.memberName(symbol))

                if (includeParameters) {
                    addAll(symbol.contextParameterSymbols.map { InternalNames.valueOrContextParameterName(it.fir) })
                    symbol.receiverParameterSymbol?.let { add(InternalNames.extensionReceiverName(it.fir)) }
                    InternalNames.dispatchReceiverName(symbol)?.let { add(it) }
                    if (symbol is FirFunctionSymbol<*>)
                        addAll(symbol.valueParameterSymbols.map { InternalNames.valueOrContextParameterName(it.fir) })
                }

                if (includeTypeParameters)
                    addAll(symbol.typeParameterSymbols.map { InternalNames.typeParameterName(it.fir) })
            }
        }

        return emptyList()
    }

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun processExportClass(call: FirFunctionCall): List<InternalName> {
        val includeTypeParameters = call.getBooleanArgument(ExportFunctionNames.includeTypeParameters, false) ?: return emptyList()
        val cls = getClassLiteral(call) ?: return emptyList()

        return buildList {
            add(InternalNames.className(cls))
            if (includeTypeParameters)
                addAll(cls.typeParameterSymbols.map { InternalNames.typeParameterName(it.fir) })
        }
    }

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun processExportAnnotation(call: FirFunctionCall): List<InternalName> {
        val cls = getClassLiteral(call) ?: return emptyList()

        if (cls.classKind != ClassKind.ANNOTATION_CLASS) {
            reporter.reportOn(call.arguments[0].source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_ANNOTATION_CLASS)
            return emptyList()
        }

        return listOf(InternalNames.annotationName(cls.fir, session))
    }

    @OptIn(SymbolInternals::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun processExportEnumEntries(call: FirFunctionCall): List<InternalName> {
        val cls = getClassLiteral(call) ?: return emptyList()

        if (cls.classKind != ClassKind.ENUM_CLASS) {
            reporter.reportOn(call.arguments[0].source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_ENUM_CLASS)
            return emptyList()
        }

        return buildList {
            add(InternalNames.className(cls))
            addAll(
                cls.collectEnumEntries(session)
                    .map { InternalNames.enumEntryName(it.fir, session) }
                    .sortedBy { it.ordinal }
            )
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun getClassLiteral(call: FirFunctionCall): FirRegularClassSymbol? {
        val arg = call.arguments[0]
        val cls = arg.extractClassFromArgument(session)

        if (cls == null) {
            reporter.reportOn(arg.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_CLASS_LITERAL)
            return null
        }

        if (!checkExportedSymbol(arg.source, cls)) {
            return null
        }

        return cls
    }

    context(context: CheckerContext, reporter: DiagnosticReporter)
    private fun FirFunctionCall.getBooleanArgument(name: Name, defaultValue: Boolean): Boolean? {
        val entries = resolvedArgumentMapping?.entries ?: error("Argument mapping is not resolved!?")
        val value = entries.firstOrNull { it.value.name == name }?.key ?: return defaultValue

        val constValue = value.evaluateAs<FirLiteralExpression>(session)?.value as? Boolean

        if (constValue == null) {
            reporter.reportOn(value.source, Errors.SYMBOL_EXPORT_REFERENCE_NOT_BOOLEAN_LITERAL)
            return null
        }

        return constValue
    }

    object Errors : BaseDiagnosticRendererFactory() {
        val SYMBOL_EXPORT_REFERENCE_MUST_BE_ANNOTATED by error0<KtDeclaration>()
        val SYMBOL_EXPORT_REFERENCE_MUST_EXTEND_BASE by error0<KtDeclaration>()
        val SYMBOL_EXPORT_REFERENCE_NOT_OBJECT by error0<KtDeclaration>()
        val SYMBOL_EXPORT_REFERENCE_NOT_INIT_BLOCK by error0<KtDeclaration>()
        val SYMBOL_EXPORT_REFERENCE_MEANINGLESS_STATEMENT by warning0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_NOT_IN_INIT_BLOCK by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_NOT_ACCESS_LITERAL by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_NOT_REFERENCE_LITERAL by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_NOT_CLASS_LITERAL by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_NOT_BOOLEAN_LITERAL by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_NOT_ANNOTATION_CLASS by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_NOT_ENUM_CLASS by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_REFERENCE_DECLARATION by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_LOCAL_DECLARATION by error0<KtExpression>()
        val SYMBOL_EXPORT_REFERENCE_EXPORTING by DiagnosticFactory1DelegateProvider<List<InternalName>>(
            Severity.INFO,
            SourceElementPositioningStrategies.DEFAULT,
            KtDeclaration::class
        )

        override val MAP = KtDiagnosticFactoryToRendererMap("SymbolExport").apply {
            put(
                SYMBOL_EXPORT_REFERENCE_MUST_BE_ANNOTATED,
                "Implementors of BaseReferenceExporter must be annotated with @ExportReferences"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_MUST_EXTEND_BASE,
                "Objects annotated with @ExportReferences must extend ${ExportFunctionNames.baseClass.asFqNameString()}"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_OBJECT,
                "@ExportReferences annotated classes must be non-anonymous objects"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_INIT_BLOCK,
                "@ExportReferences annotated objects may only have init blocks, not any other kind of declaration."
            )
            put(
                SYMBOL_EXPORT_REFERENCE_MEANINGLESS_STATEMENT,
                "Statement does not export anything and thus is meaningless."
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_IN_INIT_BLOCK,
                "Export functions can only be called directly from init blocks."
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_ACCESS_LITERAL,
                "Must be a property or function access expression"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_REFERENCE_LITERAL,
                "Must be a property or function access reference expression"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_CLASS_LITERAL,
                "Must be a class literal expression"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_BOOLEAN_LITERAL,
                "Must be a boolean literal expression"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_ANNOTATION_CLASS,
                "Class referenced by class literal is not an annotation class"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_NOT_ENUM_CLASS,
                "Class referenced by class literal is not an enum class"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_REFERENCE_DECLARATION,
                "Cannot export references to declarations of @ExportReferences classes"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_LOCAL_DECLARATION,
                "Cannot export references to local declarations"
            )
            put(
                SYMBOL_EXPORT_REFERENCE_EXPORTING,
                "Exported symbols:\n{0}",
                Renderer {
                    it.distinct().joinToString("\n") {
                        it.qualifiedName
                    }
                }
            )
        }

        init {
            RootDiagnosticRendererFactory.registerFactory(this)
        }
    }

}