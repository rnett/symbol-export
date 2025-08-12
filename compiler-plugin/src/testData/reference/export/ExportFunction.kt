package reference.export

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[kotlin, collections], classNames=[List]), name=get), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReference(List<*>::get)<!>
    }
}