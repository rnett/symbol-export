package reference.export

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_Classifier(packageName=[kotlin, collections], classNames=[List]), EXPORTED_MARKER_TypeParameter(owner=Classifier(packageName=[kotlin, collections], classNames=[List]), name=E, index=0), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportClass(List::class, true)<!>
        <!EXPORTED_MARKER_Classifier(packageName=[kotlin, collections], classNames=[Map]), EXPORTED_MARKER_TypeParameter(owner=Classifier(packageName=[kotlin, collections], classNames=[Map]), name=K, index=0), EXPORTED_MARKER_TypeParameter(owner=Classifier(packageName=[kotlin, collections], classNames=[Map]), name=V, index=1), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportClass(Map::class, includeTypeParameters = true)<!>
   }
}