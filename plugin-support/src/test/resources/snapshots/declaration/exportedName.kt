package com.example.generated

import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.PropertySymbol
import dev.rnett.symbolexport.symbol.v2.PropertySymbolDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object exportedProp : HasDeclaration<PropertySymbolDeclaration>(PropertySymbolDeclaration.Impl(PropertySymbol(QualifiedName.TopLevelCallableName(QualifiedName.PackageName(listOf("com", "example")), "prop"))))
}