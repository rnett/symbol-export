package com.rnett.symbolexport.export

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

public open class ExportExtension(objectFactory: ObjectFactory) {
    public val symbolExportOutputDirectory: DirectoryProperty = objectFactory.directoryProperty()
    public val autoAddAnnotationDependency: Property<Boolean> = objectFactory.property(Boolean::class.java)
}