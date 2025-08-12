package reference.errors

@ExportReferences
object Test : BaseReferenceExporter() {
    init {
        exportReferenced(<!SYMBOL_EXPORT_REFERENCE_NOT_ACCESS_LITERAL!>1<!>)
    }
}
