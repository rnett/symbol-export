package dev.rnett.symbolexport.export

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * Configuration for exporting symbols. Accessible at `symbolExport` in build scripts.
 *
 * @property symbolExportOutputDirectory Where the exported symbols should be stored. Has no real meaning, there is no reason to set it.
 * @property autoAddAnnotationDependency If true, the symbol export annotations dependency will automatically be added to any exported source sets.
 * @property exportFromSourceSets The source sets to export from.  If unset or empty, exports from all, which is the default.
 * @property ignoreSourceSets Do not export from these source sets. Takes precedence over [exportFromSourceSets]. May be set on the source set using [ignoreSymbols].
 * @property projectSymbolsName The project name to use for the exported symbols.
 *   If symbols from multiple projects are imported, this name will be used to distinguish them, typically as a property on the root `Symbols` object.
 *
 *   Defaults to the Gradle project name.
 *
 * @see ExportPlugin
 */
public abstract class ExportExtension {

    public abstract val symbolExportOutputDirectory: DirectoryProperty

    public abstract val autoAddAnnotationDependency: Property<Boolean>

    public abstract val exportFromSourceSets: SetProperty<String>

    public abstract val ignoreSourceSets: SetProperty<String>

    public abstract val projectSymbolsName: Property<String>
}