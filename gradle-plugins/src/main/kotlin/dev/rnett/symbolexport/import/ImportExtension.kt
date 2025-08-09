package dev.rnett.symbolexport.import

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * Configuration for importing symbols.
 *
 * @property generatedSymbolsPackage The package to use for the generated sources.  Has no real meaning, there is no reason to set it.
 * @property symbolGenerationDirectory The directory root to place the generated sources in.
 * @property attachToSourceSets  The source sets to attach the generated sources to.
 *   Default is empty, which attaches to all of them.
 * @property autoAddSymbolsDependency Whether to automatically add a dependency on the symbols library to sources sets the symbols are attached to.
 *   Defaults to true.
 * @property flattenDependencyProjects Whether to flatten all dependencies into a single symbols object.
 *   If false, it will generate a new object for each project imported, based on the project name of the exporting project.
 *    Defaults to false.
 *
 * @see ImportPlugin
 */
public abstract class ImportExtension {

    public abstract val generatedSymbolsPackage: Property<String>


    public abstract val symbolGenerationDirectory: DirectoryProperty


    public abstract val attachToSourceSets: SetProperty<String>

    public abstract val autoAddSymbolsDependency: Property<Boolean>

    public abstract val flattenDependencyProjects: Property<Boolean>
}