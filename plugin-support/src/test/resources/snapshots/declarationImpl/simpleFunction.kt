package com.example.generated

import dev.rnett.symbolexport.symbol.v2.FunctionSignature
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName
import dev.rnett.symbolexport.symbol.v2.SimpleFunctionDeclaration
import dev.rnett.symbolexport.symbol.v2.SimpleFunctionSymbol

public object Symbols {
    public object Test : HasDeclaration<SimpleFunctionDeclaration>(__declaration) {
        public object __declaration : SimpleFunctionDeclaration(SimpleFunctionSymbol(QualifiedName.TopLevelCallableName(QualifiedName.PackageName(listOf("com", "example")), "testFn"), FunctionSignature(listOf())))
    }
}