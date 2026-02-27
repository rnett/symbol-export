package com.example.generated

import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.PropertyDeclaration
import dev.rnett.symbolexport.symbol.v2.PropertySymbol
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object Test : HasDeclaration<PropertyDeclaration>(__declaration) {
        public object __declaration : PropertyDeclaration(PropertySymbol(QualifiedName.TopLevelCallableName(QualifiedName.PackageName(listOf("com", "example")), "testProp")))
    }
}