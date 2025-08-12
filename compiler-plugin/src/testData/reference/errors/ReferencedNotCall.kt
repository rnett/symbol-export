package reference.errors


@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        val a = listOf(1, 2, 3).toSet()
        exportReferenced(<!SYMBOL_EXPORT_REFERENCE_NOT_ACCESS_LITERAL!>3<!>)
        exportReferenced(<!SYMBOL_EXPORT_REFERENCE_LOCAL_DECLARATION!>a<!>)
    }
}