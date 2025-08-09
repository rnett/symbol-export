package compilertest.cases

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClass
import org.jetbrains.kotlin.ir.declarations.IrClass


class FirContext(val session: FirSession)

fun interface FirTestCase {
    fun FirContext.test(klass: FirClass)

    companion object {
        fun combine(cases: Iterable<FirTestCase>) = FirTestCase { cls ->
            cases.forEach {
                it.apply { test(cls) }
            }
        }
    }
}

class IrContext(val context: IrPluginContext)

fun interface IrTestCase {
    fun IrContext.test(klass: IrClass)

    companion object {
        fun combine(cases: Iterable<IrTestCase>) = IrTestCase { cls ->
            cases.forEach {
                it.apply { test(cls) }
            }
        }
    }
}