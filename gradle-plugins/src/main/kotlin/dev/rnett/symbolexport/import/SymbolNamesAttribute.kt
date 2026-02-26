package dev.rnett.symbolexport.import

import org.gradle.api.attributes.Attribute


public object SymbolNamesAttribute {
    public val ATTRIBUTE: Attribute<String> = Attribute.of("dev.rnett.symbol-export.symbol-names", String::class.java)
}