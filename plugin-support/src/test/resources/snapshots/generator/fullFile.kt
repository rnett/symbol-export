package com.example.generated

import dev.rnett.symbolexport.symbol.v2.ClassSymbol
import dev.rnett.symbolexport.symbol.v2.ClassSymbolDeclaration
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object TestClass : HasDeclaration<ClassSymbolDeclaration>(ClassSymbolDeclaration.Impl(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("TestClass")))))

    public object OtherClass : HasDeclaration<ClassSymbolDeclaration>(ClassSymbolDeclaration.Impl(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("OtherClass")))))
}