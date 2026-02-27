package com.example.generated

import dev.rnett.symbolexport.symbol.v2.ConstructorDeclaration
import dev.rnett.symbolexport.symbol.v2.ConstructorSymbol
import dev.rnett.symbolexport.symbol.v2.FunctionSignature
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object Test : HasDeclaration<ConstructorDeclaration>(__declaration) {
        public object __declaration :
            ConstructorDeclaration(ConstructorSymbol(QualifiedName.MemberName(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("TestClass")), "<init>"), FunctionSignature(listOf())))
    }
}