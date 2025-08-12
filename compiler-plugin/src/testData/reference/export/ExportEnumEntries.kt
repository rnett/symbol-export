package reference.export

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_Classifier(packageName=[kotlin], classNames=[DeprecationLevel]), EXPORTED_MARKER_EnumEntry(owner=Classifier(packageName=[kotlin], classNames=[DeprecationLevel]), name=ERROR, ordinal=1), EXPORTED_MARKER_EnumEntry(owner=Classifier(packageName=[kotlin], classNames=[DeprecationLevel]), name=HIDDEN, ordinal=2), EXPORTED_MARKER_EnumEntry(owner=Classifier(packageName=[kotlin], classNames=[DeprecationLevel]), name=WARNING, ordinal=0), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportEnumEntries(DeprecationLevel::class)<!>
    }
}