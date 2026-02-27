package dev.rnett.symbolexport.symbol.v2

import dev.rnett.symbolexport.symbol.SymbolExportInternals
import dev.rnett.symbolexport.symbol.annotation.AnnotationArgument
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameter
import dev.rnett.symbolexport.symbol.annotation.AnnotationParameterType
import kotlin.jvm.JvmInline

public sealed interface Declaration : SymbolDeclaration {
}

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class ClassDeclaration(symbol: ClassSymbol) : Declaration, ClassSymbolDeclaration(symbol)

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class AnnotationDeclaration(symbol: ClassSymbol) : ClassDeclaration(symbol) {
    /**
     * A representation of an annotation instance.
     *
     * @property annotation The annotation type
     * @property arguments The arguments of the annotation instance, keyed by the parameter name. All parameters are present as keys - if they are not specified, the value is null.
     */
    public abstract class Instance(public val arguments: ArgumentsMap) {
        public abstract val annotation: AnnotationDeclaration

        public inline operator fun <reified T : AnnotationArgument, P : AnnotationParameterType<T>> get(param: AnnotationParameter<P>): T? = arguments[param] as T?
        public operator fun contains(param: AnnotationParameter<*>): Boolean = param in arguments

        public operator fun get(param: String): AnnotationArgument? = arguments.getForName(param)
        public operator fun contains(param: String): Boolean = arguments.keys.any { it.name == param }
    }

    public abstract val parameters: List<AnnotationParameter<*>>

    public abstract fun produceInstance(arguments: ArgumentsMap): Instance

    @JvmInline
    public value class ArgumentsMap(private val map: Map<AnnotationParameter<*>, AnnotationArgument?>) : Map<AnnotationParameter<*>, AnnotationArgument?> by map {
        public fun getForName(name: String): AnnotationArgument? = map.entries.firstOrNull { it.key.name == name }?.value
    }
}

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class ObjectDeclaration(symbol: ClassSymbol) : ClassDeclaration(symbol)

public sealed interface CallableDeclaration : Declaration, CallableSymbolDeclaration {
    abstract override val symbol: CallableSymbol
}

public sealed interface FunctionDeclaration : Declaration, FunctionSymbolDeclaration {
    abstract override val symbol: FunctionSymbol
}

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class SimpleFunctionDeclaration(symbol: SimpleFunctionSymbol) : FunctionDeclaration, SimpleFunctionSymbolDeclaration(symbol)

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class ConstructorDeclaration(symbol: ConstructorSymbol) : FunctionDeclaration, ConstructorSymbolDeclaration(symbol)

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class PropertyDeclaration(symbol: PropertySymbol) : CallableDeclaration, PropertySymbolDeclaration(symbol)

@SubclassOptInRequired(SymbolExportInternals::class)
public abstract class EnumEntryDeclaration(symbol: EnumEntrySymbol) : Declaration, EnumEntrySymbolDeclaration(symbol)