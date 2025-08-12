package reference.export

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_TopLevelMember(packageName=[kotlin, collections], name=mapOf), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReferenced(mapOf<Int, Int>())<!>
        <!EXPORTED_MARKER_TopLevelMember(packageName=[kotlin, collections], name=toSet), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReferenced(placeholder<List<Int>>().toSet())<!>
    }
}