package com.example.generated

import dev.rnett.symbolexport.symbol.v2.ConstructorSymbol
import dev.rnett.symbolexport.symbol.v2.ConstructorSymbolDeclaration
import dev.rnett.symbolexport.symbol.v2.FunctionSignature
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object Test : HasDeclaration<ConstructorSymbolDeclaration>(
        ConstructorSymbolDeclaration.Impl(
            ConstructorSymbol(
                QualifiedName.MemberName(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("TestClass")), "<init>"),
                FunctionSignature(listOf())
            )
        )
    )
}