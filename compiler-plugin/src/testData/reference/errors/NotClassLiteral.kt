package reference.errors

@ExportReferences
object Test : BaseReferenceExporter() {
    init {
        val k = String::class
        exportClass(<!SYMBOL_EXPORT_REFERENCE_NOT_CLASS_LITERAL!>k<!>)
    }
}
