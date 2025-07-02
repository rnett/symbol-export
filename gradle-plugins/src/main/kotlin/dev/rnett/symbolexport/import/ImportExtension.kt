package dev.rnett.symbolexport.import

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public open class ImportExtension(objectFactory: ObjectFactory) {
    /**
     * The package to use for the generated sources.
     */
    public val generatedSymbolsPackage: Property<String> = objectFactory.property(String::class.java)

    /**
     * The directory root to place the generated sources in.
     */
    public val symbolGenerationDirectory: DirectoryProperty = objectFactory.directoryProperty()

    /**
     * The source sets to attach the generated sources to.
     * Default is empty, which attaches to all of them.
     */
    public val attachToSourceSets: SetProperty<String> = objectFactory.setProperty(String::class.java)

    /**
     * Whether to automatically add a dependency on the symbols library to sources sets the symbols are attached to.
     * Defaults to true.
     */
    public val autoAddSymbolsDependency: Property<Boolean> = objectFactory.property(Boolean::class.java)

    /**
     * Whether to flatten all dependencies into a single symbols object.
     * If false, it will generate a new object for each project imported, based on the project name of the exporting project.
     * Defaults to false.
     */
    public val flattenDependencyProjects: Property<Boolean> = objectFactory.property(Boolean::class.java)
}