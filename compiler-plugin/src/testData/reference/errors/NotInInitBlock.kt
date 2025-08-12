package reference.errors

@ExportReferences
object Test : BaseReferenceExporter() {
    init {
        val x = 0.apply {
            <!SYMBOL_EXPORT_REFERENCE_NOT_IN_INIT_BLOCK!>exportClass(String::class)<!>
        }
    }
}
