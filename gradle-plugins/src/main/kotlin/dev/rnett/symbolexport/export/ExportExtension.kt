package dev.rnett.symbolexport.export

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public abstract class ExportExtension {

    /**
     * Where the exported symbols should be stored. Has no real meaning, there is no reason to set it.
     */
    public abstract val symbolExportOutputDirectory: DirectoryProperty

    /**
     * If true, the symbol export annotations dependency will automatically be added to any exported source sets.
     */
    public abstract val autoAddAnnotationDependency: Property<Boolean>

    /**
     * The source sets to export from.  If unset or empty, exports from all, which is the default.
     */
    public abstract val exportFromSourceSets: SetProperty<String>

    /**
     * Do not export from these source sets.
     */
    public abstract val ignoreSourceSets: SetProperty<String>

    /**
     * The project name to use for the exported symbols.
     * If symbols from multiple projects are imported, this name will be used to distinguish them, typically as a property on the root `Symbols` object.
     *
     * Defaults to the Gradle project name.
     */
    public abstract val projectSymbolsName: Property<String>
}