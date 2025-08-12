package reference.errors

class NotAnnoClass
class NotEnum

@ExportReferences
object TestNotAnnotation : BaseReferenceExporter() {
    init {
        exportAnnotation(<!ARGUMENT_TYPE_MISMATCH, SYMBOL_EXPORT_REFERENCE_NOT_ANNOTATION_CLASS!>NotAnnoClass::class<!>)
    }
}

@ExportReferences
object TestNotEnum : BaseReferenceExporter() {
    init {
        exportEnumEntries(<!ARGUMENT_TYPE_MISMATCH, SYMBOL_EXPORT_REFERENCE_NOT_ENUM_CLASS!>NotAnnoClass::class<!>)
    }
}

@ExportReferences
object TestLocalDecl : BaseReferenceExporter() {
    init {
        class Test {}
        exportClass(<!SYMBOL_EXPORT_REFERENCE_LOCAL_DECLARATION!>Test::class<!>)
    }
}