package com.rnett.symbolexport.fir

import com.rnett.symbolexport.NameReporter
import com.rnett.symbolexport.internal.InternalName
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.error1
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.diagnostics.rendering.RenderingContext
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirPropertyChecker
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.isLocalMember
import org.jetbrains.kotlin.fir.analysis.checkers.getContainingClassSymbol
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirMemberDeclaration
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.utils.effectiveVisibility
import org.jetbrains.kotlin.fir.declarations.utils.isActual
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.packageFqName
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDeclaration


class SymbolExportCheckerExtension(session: FirSession, val reporter: NameReporter) :
    FirAdditionalCheckersExtension(session) {
    companion object {
        val EXPORT_ANNOTATION_FQN = FqName("com.rnett.symbolexport.ExportSymbol")
        val EXPORT_ANNOTATION_CLASSID = ClassId.topLevel(EXPORT_ANNOTATION_FQN)

        val PARENT_ANNOTATION_FQN = FqName("com.rnett.symbolexport.ChildrenExported")
        val PARENT_ANNOTATION_CLASSID = ClassId.topLevel(PARENT_ANNOTATION_FQN)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(
            DeclarationPredicate.create {
                annotated(EXPORT_ANNOTATION_FQN)
            }
        )
    }

    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val classCheckers: Set<FirClassChecker> = setOf(ClassChecker(reporter))
        override val functionCheckers: Set<FirFunctionChecker> = setOf(FunctionChecker(reporter))
        override val propertyCheckers: Set<FirPropertyChecker> = setOf(PropertyChecker(reporter))
    }

    abstract class BaseChecker<T : FirMemberDeclaration>(val reporter: NameReporter) :
        FirDeclarationChecker<T>(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: T) {

            if (!declaration.hasAnnotation(EXPORT_ANNOTATION_CLASSID, context.session))
                return

            if (declaration.isActual) {
                reporter.reportOn(
                    declaration.source,
                    Errors.NLG_NO_ACTUAL
                )
                return
            }

            val parent = declaration.getContainingClassSymbol()
            if (parent?.hasAnnotation(EXPORT_ANNOTATION_CLASSID, context.session) == false &&
                !parent.hasAnnotation(PARENT_ANNOTATION_CLASSID, context.session)
            ) {
                reporter.reportOn(
                    declaration.source,
                    Errors.NLG_PARENT_MUST_BE_EXPOSED
                )
                return
            }

            if (declaration.isLocalMember) {
                reporter.reportOn(
                    declaration.source,
                    Errors.NLG_NO_LOCAL_DECLARATIONS
                )
                return
            }

            if (!declaration.effectiveVisibility.publicApi) {
                reporter.reportOn(
                    declaration.source,
                    Errors.NLG_MUST_BE_PUBLIC_OR_PUBLISHED_API,
                    declaration.annotations.toString()
                )
                return
            }

            createName(declaration)?.let {
                this.reporter.reportName(it)
            }
        }

        context(context: CheckerContext)
        abstract fun createName(declaration: T): InternalName?
    }

    class ClassChecker(reporter: NameReporter) : BaseChecker<FirClass>(reporter) {
        companion object {
            fun createClassName(classSymbol: FirClassLikeSymbol<*>): InternalName.Classifier {
                return InternalName.Classifier(
                    classSymbol.classId.packageFqName.pathSegments().map { it.identifier },
                    classSymbol.classId.relativeClassName.pathSegments().map { it.identifier }
                )
            }
        }

        context(context: CheckerContext)
        override fun createName(declaration: FirClass): InternalName? {
            return createClassName(declaration.symbol)
        }
    }

    class FunctionChecker(reporter: NameReporter) : BaseChecker<FirFunction>(reporter) {
        context(context: CheckerContext)
        override fun createName(declaration: FirFunction): InternalName? {
            val parent = declaration.getContainingClassSymbol()

            return if (parent == null) {
                InternalName.TopLevelMember(
                    declaration.symbol.packageFqName().pathSegments().map { it.identifier },
                    declaration.symbol.callableId.callableName.identifier
                )
            } else {
                InternalName.ClassifierMember(
                    ClassChecker.createClassName(parent),
                    declaration.symbol.callableId.callableName.identifier
                )
            }
        }
    }

    class PropertyChecker(reporter: NameReporter) : BaseChecker<FirProperty>(reporter) {
        context(context: CheckerContext)
        override fun createName(declaration: FirProperty): InternalName? {
            val parent = declaration.getContainingClassSymbol()

            return if (parent == null) {
                InternalName.TopLevelMember(
                    declaration.symbol.packageFqName().pathSegments().map { it.identifier },
                    declaration.symbol.callableId.callableName.identifier
                )
            } else {
                InternalName.ClassifierMember(
                    ClassChecker.createClassName(parent),
                    declaration.symbol.callableId.callableName.identifier
                )
            }
        }
    }


    object Errors : BaseDiagnosticRendererFactory() {
        val NLG_PARENT_MUST_BE_EXPOSED by error0<KtDeclaration>()
        val NLG_NO_LOCAL_DECLARATIONS by error0<KtDeclaration>()
        val NLG_MUST_BE_PUBLIC_OR_PUBLISHED_API by error1<KtDeclaration, String>()
        val NLG_NO_ACTUAL by error0<KtDeclaration>()

        override val MAP = KtDiagnosticFactoryToRendererMap("SymbolExport").apply {
            put(
                NLG_PARENT_MUST_BE_EXPOSED,
                "Parents of '@ExportSymbol' declarations must also be annotated with '@ExportSymbol' or '@ChildrenExported'."
            )
            put(
                NLG_NO_LOCAL_DECLARATIONS,
                "'@ExportSymbol' on local declarations is not supported."
            )
            put(
                NLG_MUST_BE_PUBLIC_OR_PUBLISHED_API,
                "'@ExportSymbol' declarations must be 'public' or '@PublishedApi internal' {0}.",
                object : DiagnosticParameterRenderer<String> {
                    override fun render(
                        obj: String,
                        renderingContext: RenderingContext
                    ): String {
                        return obj
                    }

                }
            )
            put(
                NLG_NO_ACTUAL,
                "'@ExportSymbol' on actual declarations is not supported, annotate the expect declaration instead."
            )
        }

        init {
            RootDiagnosticRendererFactory.registerFactory(this)
        }
    }
}
