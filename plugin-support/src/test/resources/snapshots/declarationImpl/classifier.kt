package com.example.generated

import dev.rnett.symbolexport.symbol.v2.ClassDeclaration
import dev.rnett.symbolexport.symbol.v2.ClassSymbol
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object Test : HasDeclaration<ClassDeclaration>(__declaration) {
        public object __declaration : ClassDeclaration(ClassSymbol(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("TestClass"))))
    }
}