package dev.rnett.symbolexport.fir

import dev.rnett.symbolexport.NameReporter
import dev.rnett.symbolexport.internal.InternalName
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.CONTEXT
import dev.rnett.symbolexport.internal.InternalName.IndexedParameter.Type.VALUE
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.DISPATCH
import dev.rnett.symbolexport.internal.InternalName.ReceiverParameter.Type.EXTENSION
import dev.rnett.symbolexport.internal.ParameterType
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.Renderer
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.correspondingValueParameterFromPrimaryConstructor
import org.jetbrains.kotlin.fir.declarations.utils.effectiveVisibility
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.dispatchReceiverClassTypeOrNull
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.resolve.transformers.publishedApiEffectiveVisibility
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.customAnnotations
import org.jetbrains.kotlin.fir.types.typeAnnotations
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.psi.KtDeclaration
import java.util.concurrent.ConcurrentHashMap

class SymbolExportCheckerExtension(session: FirSession, val warnOnExported: Boolean, val nameReporter: NameReporter) :
    FirAdditionalCheckersExtension(session) {
    companion object {
        val EXPORT_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportSymbol")
        val EXPORT_ANNOTATION_CLASSID = ClassId.topLevel(EXPORT_ANNOTATION_FQN)

        val exportPredicate = DeclarationPredicate.create {
            annotated(EXPORT_ANNOTATION_FQN)
        }

        val PARENT_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ChildrenExported")
        val PARENT_ANNOTATION_CLASSID = ClassId.topLevel(PARENT_ANNOTATION_FQN)

        val childrenExportedPredicate = DeclarationPredicate.create {
            annotated(PARENT_ANNOTATION_FQN)
        }

        val EXPORT_RECEIVERS_ANNOTATION_FQN = FqName("dev.rnett.symbolexport.ExportReceivers")
        val EXPORT_RECEIVERS_ANNOTATION_CLASSID = ClassId.topLevel(EXPORT_RECEIVERS_ANNOTATION_FQN)


        val EXPORT_RECEIVERS_DISPATCH_PROP = Name.identifier("dispatch")
        val EXPORT_RECEIVERS_EXTENSION_PROP = Name.identifier("extension")

        fun createClassName(classSymbol: FirClassLikeSymbol<*>): InternalName.Classifier {
            return InternalName.Classifier(
                classSymbol.classId.packageFqName.pathSegments().map { it.asString() },
                classSymbol.classId.relativeClassName.pathSegments().map { it.asString() }
            )
        }

        fun createMemberName(symbol: FirCallableSymbol<*>, overrideName: Name? = null): InternalName.Member {
            val parent = symbol.getContainingClassSymbol()

            if (symbol is FirConstructorSymbol) {
                return InternalName.Constructor(createClassName(parent!!), SpecialNames.INIT.asString())
            }

            return if (parent == null) {
                InternalName.TopLevelMember(
                    symbol.packageFqName().pathSegments().map { it.asString() },
                    overrideName?.asString() ?: symbol.name.asString()
                )
            } else {
                InternalName.ClassifierMember(
                    createClassName(parent),
                    overrideName?.asString() ?: symbol.name.asString()
                )
            }
        }

        /**
         * The index offset for the type, according to the Kotlin compiler's parameter ordering:
         * `[dispatch receiver, context parameters, extension receiver, value parameters]`.
         */
        fun getIndexStartOffset(symbol: FirCallableSymbol<*>, type: ParameterType): Int {
            return when (type) {
                DISPATCH -> 0
                CONTEXT -> if (symbol.dispatchReceiverType != null) 1 else 0
                EXTENSION -> getIndexStartOffset(symbol, CONTEXT) + symbol.contextParameterSymbols.size
                VALUE -> getIndexStartOffset(symbol, EXTENSION) + if (symbol.receiverParameterSymbol != null) 1 else 0
            }
        }
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(
            exportPredicate,
            childrenExportedPredicate,
        )
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        private val illegalUseChecker = IllegalUseChecker()
        private val extensionReceiverChecker = ExtensionReceiverChecker(illegalUseChecker)

        override val basicDeclarationCheckers: Set<FirBasicDeclarationChecker> = setOf(
            illegalUseChecker
        )

        override val classCheckers: Set<FirClassChecker> = setOf(
            ClassChecker(illegalUseChecker)
        )
        override val callableDeclarationCheckers: Set<FirCallableDeclarationChecker> = setOf(
            MemberChecker(illegalUseChecker),
            DispatchReceiverChecker(illegalUseChecker),
            MemberExtensionReceiverChecker(extensionReceiverChecker)
        )

        override val valueParameterCheckers: Set<FirValueParameterChecker> = setOf(
            ValueParameterChecker(illegalUseChecker)
        )
        override val typeParameterCheckers: Set<FirTypeParameterChecker> = setOf(
            TypeParameterChecker(illegalUseChecker)
        )
        override val enumEntryCheckers: Set<FirEnumEntryChecker> = setOf(
            EnumEntryChecker(illegalUseChecker)
        )
        //TODO this never gets called. Switch to it once it works
//        override val receiverParameterCheckers: Set<FirReceiverParameterChecker> = setOf(ExtensionReceiverChecker())
    }

    inner class IllegalUseChecker() : FirBasicDeclarationChecker(MppCheckerKind.Common) {

        private val illegalUses = ConcurrentHashMap<FirBasedSymbol<*>, Boolean?>()

        @OptIn(SymbolInternals::class)
        context(context: CheckerContext, reporter: DiagnosticReporter)
        fun isIllegalUse(declaration: FirDeclaration): Boolean = illegalUses.computeIfAbsent(declaration.symbol) { checkIllegalUse(declaration) } ?: false

        context(context: CheckerContext, reporter: DiagnosticReporter)
        private fun checkIllegalUse(declaration: FirDeclaration): Boolean? {
            if (
                !session.predicateBasedProvider.matches(exportPredicate, declaration) &&
                !declaration.hasAnnotation(EXPORT_ANNOTATION_CLASSID, session) &&
                !session.predicateBasedProvider.matches(childrenExportedPredicate, declaration) &&
                !declaration.hasAnnotation(PARENT_ANNOTATION_CLASSID, session)
            )
                return null

            if (declaration.isLocalMember) {
                reporter.reportOn(
                    declaration.source,
                    Errors.SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS
                )
                return true
            }

            if (declaration is FirMemberDeclaration) {
                val visibility = declaration.publishedApiEffectiveVisibility ?: declaration.effectiveVisibility

                if (!visibility.publicApi) {
                    reporter.reportOn(
                        declaration.source,
                        Errors.SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API,
                        declaration.effectiveVisibility.toString()
                    )
                    return true
                }
            }
            return false
        }

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirDeclaration) {
            isIllegalUse(declaration)
        }
    }

    abstract inner class BaseDeclarationChecker<T : FirDeclaration>(val illegalUseChecker: IllegalUseChecker) :
        FirDeclarationChecker<T>(MppCheckerKind.Common) {

        abstract fun getParent(declaration: T): FirBasedSymbol<*>?

        context(context: CheckerContext)
        open fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: T): Boolean = hasExportAnnotation


        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: T) {

            if (declaration is FirMemberDeclaration && declaration.isActual) {
                return
            }

            val isAnnotated = session.predicateBasedProvider.matches(exportPredicate, declaration)

            if (!shouldExportFrom(isAnnotated, declaration)) {
                return
            }

            if (illegalUseChecker.isIllegalUse(declaration)) {
                return
            }

            val parent = getParent(declaration)
            if (parent != null) {
                if (
                    !session.predicateBasedProvider.matches(childrenExportedPredicate, parent) &&
                    !session.predicateBasedProvider.matches(exportPredicate, parent)
                ) {
                    reporter.reportOn(
                        parent.source,
                        Errors.SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED
                    )
                    return
                }
            }

            createName(declaration)?.let { (source, name) ->
                nameReporter.reportName(name)
                if (warnOnExported) {
                    reporter.reportOn(source, Errors.symbolExportMarker(name), name)
                }
            }
        }

        context(context: CheckerContext, reporter: DiagnosticReporter)
        abstract fun createName(declaration: T): Pair<KtSourceElement?, InternalName>?
    }

    inner class ClassChecker(illegalUseChecker: IllegalUseChecker) : BaseDeclarationChecker<FirClass>(illegalUseChecker) {
        override fun getParent(declaration: FirClass): FirBasedSymbol<*>? = declaration.getContainingClassSymbol()

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun createName(declaration: FirClass): Pair<KtSourceElement?, InternalName>? {
            return declaration.source to createClassName(declaration.symbol)
        }
    }

    inner class MemberChecker<T : FirCallableDeclaration>(illegalUseChecker: IllegalUseChecker) : BaseDeclarationChecker<T>(illegalUseChecker) {
        override fun getParent(declaration: T): FirBasedSymbol<*>? = declaration.getContainingClassSymbol()

        context(context: CheckerContext)
        override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: T): Boolean {

            // do not include enum entries, fields, params, etc
            if (declaration !is FirProperty && declaration !is FirFunction) return false

            if (hasExportAnnotation) return true

            if (declaration is FirProperty) {
                val valueParam = declaration.correspondingValueParameterFromPrimaryConstructor ?: return false
                return context.session.predicateBasedProvider.matches(exportPredicate, valueParam)
            }
            return false
        }

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun createName(declaration: T): Pair<KtSourceElement?, InternalName>? {
            return declaration.source to createMemberName(declaration.symbol)
        }
    }

    inner class ValueParameterChecker(illegalUseChecker: IllegalUseChecker) : BaseDeclarationChecker<FirValueParameter>(illegalUseChecker) {
        override fun getParent(declaration: FirValueParameter): FirBasedSymbol<*>? =
            declaration.containingDeclarationSymbol

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun createName(declaration: FirValueParameter): Pair<KtSourceElement?, InternalName>? {
            val parent = declaration.symbol.containingDeclarationSymbol as FirCallableSymbol<*>
            val parentName = createMemberName(parent)

            val type = when (declaration.valueParameterKind) {
                FirValueParameterKind.Regular -> VALUE
                FirValueParameterKind.ContextParameter -> CONTEXT
                FirValueParameterKind.LegacyContextReceiver -> {
                    return null
                }
            }

            val indexInList = when (type) {
                VALUE -> {
                    parent as FirFunctionSymbol<*>
                    parent.valueParameterSymbols.indexOf(declaration.symbol)
                }

                CONTEXT -> parent.contextParameterSymbols.indexOf(declaration.symbol)
            }
            if (indexInList < 0) error("Could not find $declaration in $parent")

            return declaration.source to
                    InternalName.IndexedParameter(
                        parentName,
                        declaration.name.asString(),
                        getIndexStartOffset(parent, type) + indexInList,
                        indexInList,
                        type
                    )
        }
    }

    inner class ExtensionReceiverChecker(illegalUseChecker: IllegalUseChecker) : BaseDeclarationChecker<FirReceiverParameter>(illegalUseChecker) {
        override fun getParent(declaration: FirReceiverParameter): FirBasedSymbol<*>? =
            declaration.containingDeclarationSymbol

        context(context: CheckerContext)
        override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirReceiverParameter): Boolean {
            if (hasExportAnnotation)
                return true

            if (declaration.hasAnnotation(EXPORT_ANNOTATION_CLASSID, session))
                return true

            if (declaration.typeRef.hasAnnotation(EXPORT_ANNOTATION_CLASSID, session))
                return true

            val exportReceiverAnnotation = declaration.containingDeclarationSymbol.getAnnotationByClassId(
                EXPORT_RECEIVERS_ANNOTATION_CLASSID,
                session
            )

            if (exportReceiverAnnotation != null) {
                if (exportReceiverAnnotation.getBooleanArgument(EXPORT_RECEIVERS_EXTENSION_PROP, session) != false)
                    return true

            }
            return false
        }

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun createName(declaration: FirReceiverParameter): Pair<KtSourceElement?, InternalName>? {
            val parent = declaration.symbol.containingDeclarationSymbol as FirCallableSymbol<*>
            val parentName = createMemberName(parent)

            return declaration.source to InternalName.ReceiverParameter(
                parentName,
                SpecialNames.RECEIVER.asString(),
                getIndexStartOffset(parent, EXTENSION),
                EXTENSION
            )
        }
    }

    inner class MemberExtensionReceiverChecker<T : FirCallableDeclaration>(val extensionReceiverChecker: ExtensionReceiverChecker) : FirDeclarationChecker<T>(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: T) {
            declaration.receiverParameter?.let { extensionReceiverChecker.check(it) }
        }
    }

    inner class DispatchReceiverChecker<T : FirCallableDeclaration>(illegalUseChecker: IllegalUseChecker) : BaseDeclarationChecker<T>(illegalUseChecker) {
        // we're exporting a child
        override fun getParent(declaration: T): FirBasedSymbol<*>? = declaration.symbol

        context(context: CheckerContext)
        override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: T): Boolean {

            // we ignore the export annotation here, since it's on the function, not the receiver

            val receiver = declaration.dispatchReceiverClassTypeOrNull() ?: return false


            if (receiver.typeAnnotations.any { it.toAnnotationClassId(session) == EXPORT_ANNOTATION_CLASSID } || receiver.customAnnotations.any {
                    it.toAnnotationClassId(
                        session
                    ) == EXPORT_ANNOTATION_CLASSID
                })
                return true


            val exportReceiverAnnotation = declaration.getAnnotationByClassId(
                EXPORT_RECEIVERS_ANNOTATION_CLASSID,
                session
            )

            if (exportReceiverAnnotation != null) {
                if (exportReceiverAnnotation.getBooleanArgument(EXPORT_RECEIVERS_DISPATCH_PROP, session) != false)
                    return true

            }
            return false
        }

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun createName(declaration: T): Pair<KtSourceElement?, InternalName>? {
            val parentName = createMemberName(declaration.symbol)

            return declaration.source to InternalName.ReceiverParameter(
                parentName,
                SpecialNames.THIS.asString(),
                getIndexStartOffset(declaration.symbol, DISPATCH),
                DISPATCH
            )
        }
    }

    inner class TypeParameterChecker(illegalUseChecker: IllegalUseChecker) : BaseDeclarationChecker<FirTypeParameter>(illegalUseChecker) {
        context(context: CheckerContext)
        override fun shouldExportFrom(hasExportAnnotation: Boolean, declaration: FirTypeParameter): Boolean {
            return hasExportAnnotation || declaration.hasAnnotation(EXPORT_ANNOTATION_CLASSID, session)
        }

        override fun getParent(declaration: FirTypeParameter): FirBasedSymbol<*>? =
            declaration.containingDeclarationSymbol

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun createName(declaration: FirTypeParameter): Pair<KtSourceElement?, InternalName>? {
            val parent = declaration.symbol.containingDeclarationSymbol

            val parentName: InternalName
            val indexInList: Int

            if (parent is FirCallableSymbol<*>) {
                parentName = createMemberName(parent)
                indexInList = parent.typeParameterSymbols.indexOf(declaration.symbol)
            } else if (parent is FirClassSymbol<*>) {
                parentName = createClassName(parent)
                indexInList = parent.typeParameterSymbols.indexOf(declaration.symbol)
            } else {
                return null
            }

            if (indexInList < 0) error("Could not find $declaration in $parent")

            return declaration.source to InternalName.TypeParameter(
                parentName,
                declaration.name.asString(),
                indexInList
            )
        }
    }

    inner class EnumEntryChecker(illegalUseChecker: IllegalUseChecker) : BaseDeclarationChecker<FirEnumEntry>(illegalUseChecker) {
        override fun getParent(declaration: FirEnumEntry): FirBasedSymbol<*>? =
            declaration.getContainingClassSymbol()

        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun createName(declaration: FirEnumEntry): Pair<KtSourceElement?, InternalName>? {

            val ordinal = (declaration.getContainingClassSymbol()!! as FirClassSymbol<*>).collectEnumEntries(session).indexOf(declaration.symbol)

            if (ordinal < 0) error("Could not find $declaration in ${declaration.getContainingClassSymbol()}")

            return declaration.source to InternalName.EnumEntry(createClassName(declaration.getContainingClassSymbol()!!), declaration.name.asString(), ordinal)
        }
    }

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
}
