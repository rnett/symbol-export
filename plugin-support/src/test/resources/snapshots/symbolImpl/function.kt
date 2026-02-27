package com.example.generated

import dev.rnett.symbolexport.symbol.v2.FunctionSignature
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName
import dev.rnett.symbolexport.symbol.v2.SimpleFunctionSymbol
import dev.rnett.symbolexport.symbol.v2.SimpleFunctionSymbolDeclaration

public object Symbols {
    public object Test : HasDeclaration<SimpleFunctionSymbolDeclaration>(
        SimpleFunctionSymbolDeclaration.Impl(
            SimpleFunctionSymbol(
                QualifiedName.TopLevelCallableName(QualifiedName.PackageName(listOf("com", "example")), "testFunc"),
                FunctionSignature(listOf())
            )
        )
    )
}