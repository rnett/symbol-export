package reference.errors

@ExportReferences
object Test : BaseReferenceExporter() {
    init {
        val b = true
        exportClass(String::class, includeTypeParameters = <!SYMBOL_EXPORT_REFERENCE_NOT_BOOLEAN_LITERAL!>b<!>)
    }
}
