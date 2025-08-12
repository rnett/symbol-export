package reference.export

context(l: Long)
fun String.test(a: Int, b: String) {

}

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[reference, export], name=test), name=a, index=2, indexInList=0, type=VALUE), EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[reference, export], name=test), name=b, index=3, indexInList=1, type=VALUE), EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[reference, export], name=test), name=l, index=0, indexInList=0, type=CONTEXT), EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[reference, export], name=test), name=<receiver>, index=1, type=EXTENSION), EXPORTED_MARKER_TopLevelMember(packageName=[reference, export], name=test), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReference(String::<!CALLABLE_REFERENCE_TO_CONTEXTUAL_DECLARATION!>test<!>, true, true)<!>
    }
}