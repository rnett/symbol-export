package reference.export

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_Classifier(packageName=[kotlin, collections], classNames=[List]), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportClass(List::class)<!>
    }
}