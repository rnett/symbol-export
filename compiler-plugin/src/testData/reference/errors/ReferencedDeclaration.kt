package reference.errors

@ExportReferences
object TestReferenceDeclaration : BaseReferenceExporter() {
    <!SYMBOL_EXPORT_REFERENCE_NOT_INIT_BLOCK!>fun declaredHere() {}<!>
    init {
        exportReference(<!SYMBOL_EXPORT_REFERENCE_REFERENCE_DECLARATION!>::declaredHere<!>)
        exportReference(<!SYMBOL_EXPORT_REFERENCE_REFERENCE_DECLARATION!>::exportReferenced<!>)
        exportReferenced(<!SYMBOL_EXPORT_REFERENCE_NOT_IN_INIT_BLOCK, SYMBOL_EXPORT_REFERENCE_REFERENCE_DECLARATION!>exportReference(::exportReferenced)<!>)
    }
}
