package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import dev.rnett.symbolexport.internal.InternalDeclaration
import dev.rnett.symbolexport.internal.InternalSymbol
import org.junit.jupiter.api.Test

class DeclarationImplGeneratorTest : AbstractGeneratorSnapshotTest() {

    @Test
    fun testClassifier() {
        val symbol = InternalSymbol.Classifier(listOf("com", "example"), listOf("TestClass"))
        val declaration = InternalDeclaration.Classifier(
            symbol,
            emptyList(),
            null,
            null,
            isConcrete = true,
            isObject = false
        )
        val builder = TypeSpec.objectBuilder("Test")
        DeclarationImplGenerator.addDeclarationInstance(builder, ClassName("com.example", "Test"), declaration)
        assertSnapshot(builder.build(), "declarationImpl/classifier")
    }

    @Test
    fun testConstructor() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), listOf("TestClass"), Names.INIT, emptyList(), null)
        val declaration = InternalDeclaration.Constructor(
            symbol,
            classTypeParams = emptyList(),
            parameters = emptyList()
        )
        val builder = TypeSpec.objectBuilder("Test")
        DeclarationImplGenerator.addDeclarationInstance(builder, ClassName("com.example", "Test"), declaration)
        assertSnapshot(builder.build(), "declarationImpl/constructor")
    }

    @Test
    fun testSimpleFunction() {
        val symbol = InternalSymbol.Function(listOf("com", "example"), null, "testFn", emptyList(), null)
        val declaration = InternalDeclaration.SimpleFunction(
            symbol,
            typeParams = emptyList(),
            parameters = emptyList(),
            isAccessor = false
        )
        val builder = TypeSpec.objectBuilder("Test")
        DeclarationImplGenerator.addDeclarationInstance(builder, ClassName("com.example", "Test"), declaration)
        assertSnapshot(builder.build(), "declarationImpl/simpleFunction")
    }

    @Test
    fun testProperty() {
        val symbol = InternalSymbol.Property(listOf("com", "example"), null, "testProp", null)
        val getterSymbol = InternalSymbol.Function(listOf("com", "example"), null, "getTestProp", emptyList(), null)
        val getter = InternalDeclaration.SimpleFunction(
            getterSymbol,
            typeParams = emptyList(),
            parameters = emptyList(),
            isAccessor = true
        )
        val declaration = InternalDeclaration.Property(
            symbol,
            getter = getter,
            setter = null
        )
        val builder = TypeSpec.objectBuilder("Test")
        DeclarationImplGenerator.addDeclarationInstance(builder, ClassName("com.example", "Test"), declaration)
        assertSnapshot(builder.build(), "declarationImpl/property")
    }
}
