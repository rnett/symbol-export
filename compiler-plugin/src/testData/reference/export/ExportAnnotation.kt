package reference.export

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_Annotation(packageName=[kotlin], classNames=[Deprecated], parameters=[Parameter(name=message, index=0, type=STRING), Parameter(name=replaceWith, index=1, type=Annotation(annotationClass=Classifier(packageName=[kotlin], classNames=[ReplaceWith]))), Parameter(name=level, index=2, type=Enum(enumClass=Classifier(packageName=[kotlin], classNames=[DeprecationLevel])))]), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportAnnotation(Deprecated::class)<!>
    }
}