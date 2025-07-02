package dev.rnett.symbolexport.export

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

public open class ExportExtension(objectFactory: ObjectFactory) {

    /**
     * Where the exported symbols should be stored. Has no real meaning, there is no reason to set it.
     */
    public val symbolExportOutputDirectory: DirectoryProperty = objectFactory.directoryProperty()

    /**
     * If true, the symbol export annotations dependency will automatically be added to any exported source sets.
     */
    public val autoAddAnnotationDependency: Property<Boolean> = objectFactory.property(Boolean::class.java)

    /**
     * The source sets to export from.  If unset or empty, exports from all, which is the default.
     */
    public val exportFromSourceSets: SetProperty<String> = objectFactory.setProperty(String::class.java)

    /**
     * Do not export from these source sets.
     */
    public val ignoreSourceSets: SetProperty<String> = objectFactory.setProperty(String::class.java)
}