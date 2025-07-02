package dev.rnett.symbolexport.import

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public open class ImportExtension(objectFactory: ObjectFactory) {
    public val generatedSymbolsPackage: Property<String> = objectFactory.property(String::class.java)
    public val symbolGenerationDirectory: DirectoryProperty = objectFactory.directoryProperty()
    public val attachToSourceSets: SetProperty<String> = objectFactory.setProperty(String::class.java)
    public val autoAddSymbolsDependency: Property<Boolean> = objectFactory.property(Boolean::class.java)
}