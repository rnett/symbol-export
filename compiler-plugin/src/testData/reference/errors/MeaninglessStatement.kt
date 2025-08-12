package reference.errors

@ExportReferences
object Test : BaseReferenceExporter() {
    init {
        <!SYMBOL_EXPORT_REFERENCE_MEANINGLESS_STATEMENT!>placeholder<String>()<!>
    }
}
