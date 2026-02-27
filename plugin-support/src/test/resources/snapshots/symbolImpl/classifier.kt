package com.example.generated

import dev.rnett.symbolexport.symbol.v2.ClassSymbol
import dev.rnett.symbolexport.symbol.v2.ClassSymbolDeclaration
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object Test : HasDeclaration<ClassSymbolDeclaration>(ClassSymbolDeclaration.Impl(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("TestClass")))))
}