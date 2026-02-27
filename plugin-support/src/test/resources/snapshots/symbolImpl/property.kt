package com.example.generated

import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.PropertySymbol
import dev.rnett.symbolexport.symbol.v2.PropertySymbolDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object Test :
        HasDeclaration<PropertySymbolDeclaration>(PropertySymbolDeclaration.Impl(PropertySymbol(QualifiedName.MemberName(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("TestClass")), "testProp"))))
}