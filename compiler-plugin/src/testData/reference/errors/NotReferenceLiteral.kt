package reference.errors

fun foo() {}

@ExportReferences
object Test : BaseReferenceExporter() {
    init {
        val r = ::foo
        exportReference(<!SYMBOL_EXPORT_REFERENCE_NOT_REFERENCE_LITERAL!>r<!>)
    }
}
