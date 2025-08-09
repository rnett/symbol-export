package test.compiler

import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirClassChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.utils.nameOrSpecialName
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import test.cases.Cases
import test.cases.FirContext

@OptIn(ExperimentalCompilerApi::class)
class FirRegistrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::Transformer
        +::Checker
    }
}

private class Transformer(session: FirSession) : FirStatusTransformerExtension(session) {
    val context = FirContext(session)
    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
        return true
    }

    override fun transformStatus(status: FirDeclarationStatus, regularClass: FirRegularClass, containingClass: FirClassLikeSymbol<*>?, isLocal: Boolean): FirDeclarationStatus {
        Cases.casesFor(regularClass.name.asString()).forEach {
            it.firStatus.apply { context.test(regularClass) }
        }
        return super.transformStatus(status, regularClass, containingClass, isLocal)
    }
}

private class Checker(session: FirSession) : FirAdditionalCheckersExtension(session) {
    val firContext = FirContext(session)
    override val declarationCheckers: DeclarationCheckers = object : DeclarationCheckers() {
        override val classCheckers: Set<FirClassChecker> = setOf(ClassChecker())
    }

    inner class ClassChecker : FirClassChecker(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(declaration: FirClass) {
            Cases.casesFor(declaration.nameOrSpecialName.asString()).forEach {
                it.firCheckers.apply { firContext.test(declaration) }
            }
        }
    }
}