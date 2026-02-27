package com.example.generated

import dev.rnett.symbolexport.symbol.v2.FunctionSignature

public object Symbols {
    public object Generated {
        public fun wrapper() {
            FunctionSignature(
                listOf(
                    dev.rnett.symbolexport.symbol.v2.FunctionSignature.ParamSignature(
                        "p1",
                        false,
                        dev.rnett.symbolexport.symbol.v2.FunctionSignature.TypeSignature.ClassBased(
                            "kotlin.collections.List",
                            false,
                            listOf(
                                dev.rnett.symbolexport.symbol.v2.FunctionSignature.TypeArgumentSignature.Projection(
                                    dev.rnett.symbolexport.symbol.v2.FunctionSignature.TypeSignature.ClassBased("kotlin.Number", false, listOf()),
                                    dev.rnett.symbolexport.symbol.v2.FunctionSignature.TypeArgumentSignature.Variance.OUT
                                )
                            )
                        )
                    )
                )
            )
        }
    }
}