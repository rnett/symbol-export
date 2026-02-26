package dev.rnett.symbolexport.import

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * Configuration for importing symbols. Accessible at `symbolImport` in build scripts.
 *
 * @property generatedSymbolsPackage The package to use for the generated sources. Defaults to `symbols`.
 * @property symbolGenerationDirectory The directory root to place the generated sources in. Purely internal, there is no reason to set this.
 * @property attachToSourceSets  The source sets to attach the generated sources to.
 *   Default is empty, which attaches to all of them.
 * @property autoAddSymbolsDependency Whether to automatically add a dependency on the symbols library to sources sets the symbols are attached to.
 *   Defaults to true.
 *
 * @see ImportPlugin
 */
public abstract class ImportExtension {

    public abstract val generatedSymbolsPackage: Property<String>


    public abstract val symbolGenerationDirectory: DirectoryProperty


    public abstract val attachToSourceSets: SetProperty<String>

    public abstract val autoAddSymbolsDependency: Property<Boolean>

    internal abstract val importedSymbols: MapProperty<String, Any>

    /**
     * Adds a symbol import dependency.
     * [dependencyNotation] has the same rules as [DependencyHandler]'s `dependencyNotation` parameters (and will be passed as one, eventually).
     *
     * Dependencies are resolved independently, with no conflict resolution or anything of the sort between them.
     * They are mapped by [name], so adding a dependency with an already used name will overwrite it.
     */
    public fun import(name: String, dependencyNotation: Any) {
        this.importedSymbols.put(name, dependencyNotation)
    }
}