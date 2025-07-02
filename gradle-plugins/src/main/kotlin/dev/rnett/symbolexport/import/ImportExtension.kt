package dev.rnett.symbolexport.import

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public abstract class ImportExtension {
    /**
     * The package to use for the generated sources.
     */
    public abstract val generatedSymbolsPackage: Property<String>

    /**
     * The directory root to place the generated sources in.
     */
    public abstract val symbolGenerationDirectory: DirectoryProperty

    /**
     * The source sets to attach the generated sources to.
     * Default is empty, which attaches to all of them.
     */
    public abstract val attachToSourceSets: SetProperty<String>

    /**
     * Whether to automatically add a dependency on the symbols library to sources sets the symbols are attached to.
     * Defaults to true.
     */
    public abstract val autoAddSymbolsDependency: Property<Boolean>

    /**
     * Whether to flatten all dependencies into a single symbols object.
     * If false, it will generate a new object for each project imported, based on the project name of the exporting project.
     * Defaults to false.
     */
    public abstract val flattenDependencyProjects: Property<Boolean>
}