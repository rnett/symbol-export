package com.example.generated

import dev.rnett.symbolexport.symbol.v2.EnumEntrySymbol
import dev.rnett.symbolexport.symbol.v2.EnumEntrySymbolDeclaration
import dev.rnett.symbolexport.symbol.v2.HasDeclaration
import dev.rnett.symbolexport.symbol.v2.QualifiedName

public object Symbols {
    public object Test :
        HasDeclaration<EnumEntrySymbolDeclaration>(EnumEntrySymbolDeclaration.Impl(EnumEntrySymbol(QualifiedName.MemberName(QualifiedName.ClassName(QualifiedName.PackageName(listOf("com", "example")), listOf("TestEnum")), "ENTRY"))))
}