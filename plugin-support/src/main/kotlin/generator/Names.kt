package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.ClassName

internal object Names {
    val packageName = "dev.rnett.symbolexport.symbol.v2"

    val Symbol = ClassName(packageName, "Symbol")
    val ClassSymbol = ClassName(packageName, "ClassSymbol")
    val CallableSymbol = ClassName(packageName, "CallableSymbol")
    val FunctionSymbol = ClassName(packageName, "FunctionSymbol")
    val SimpleFunctionSymbol = ClassName(packageName, "SimpleFunctionSymbol")
    val ConstructorSymbol = ClassName(packageName, "ConstructorSymbol")
    val PropertySymbol = ClassName(packageName, "PropertySymbol")
    val EnumEntrySymbol = ClassName(packageName, "EnumEntrySymbol")

    val ClassDeclaration = ClassName(packageName, "ClassDeclaration")
    val SimpleFunctionDeclaration = ClassName(packageName, "SimpleFunctionDeclaration")
    val ConstructorDeclaration = ClassName(packageName, "ConstructorDeclaration")
    val PropertyDeclaration = ClassName(packageName, "PropertyDeclaration")
    val EnumEntryDeclaration = ClassName(packageName, "EnumEntryDeclaration")

    val ClassSymbolDeclaration = ClassName(packageName, "ClassSymbolDeclaration")
    val SimpleFunctionSymbolDeclaration = ClassName(packageName, "SimpleFunctionSymbolDeclaration")
    val ConstructorSymbolDeclaration = ClassName(packageName, "ConstructorSymbolDeclaration")
    val PropertySymbolDeclaration = ClassName(packageName, "PropertySymbolDeclaration")
    val EnumEntrySymbolDeclaration = ClassName(packageName, "EnumEntrySymbolDeclaration")

    val HasDeclaration = ClassName(packageName, "HasDeclaration")
    val INIT = "<init>"

    val QualifiedName = ClassName(packageName, "QualifiedName")
    val PackageName = ClassName(packageName, "QualifiedName", "PackageName")
    val ClassName = ClassName(packageName, "QualifiedName", "ClassName")
    val CallableName = ClassName(packageName, "QualifiedName", "CallableName")
    val MemberName = ClassName(packageName, "QualifiedName", "MemberName")
    val TopLevelCallableName = ClassName(packageName, "QualifiedName", "TopLevelCallableName")

    val FunctionSignature = ClassName(packageName, "FunctionSignature")
    val ParamSignature = ClassName(packageName, "FunctionSignature", "ParamSignature")
    val TypeSignature = ClassName(packageName, "FunctionSignature", "TypeSignature")
    val ClassBased = ClassName(packageName, "FunctionSignature", "TypeSignature", "ClassBased")
    val TypeParam = ClassName(packageName, "FunctionSignature", "TypeSignature", "TypeParam")
    val Dynamic = ClassName(packageName, "FunctionSignature", "TypeSignature", "Dynamic")
    val TypeArgumentSignature = ClassName(packageName, "FunctionSignature", "TypeArgumentSignature")
    val Projection = ClassName(packageName, "FunctionSignature", "TypeArgumentSignature", "Projection")
    val Wildcard = ClassName(packageName, "FunctionSignature", "TypeArgumentSignature", "Wildcard")
    val Variance = ClassName(packageName, "FunctionSignature", "TypeArgumentSignature", "Variance")
}