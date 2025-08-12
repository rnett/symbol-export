package reference.export

context(l: Long)
val <T> T.test get() = 4

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[reference, export], name=test), name=l, index=0, indexInList=0, type=CONTEXT), EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[reference, export], name=test), name=<receiver>, index=1, type=EXTENSION), EXPORTED_MARKER_TopLevelMember(packageName=[reference, export], name=test), EXPORTED_MARKER_TypeParameter(owner=TopLevelMember(packageName=[reference, export], name=test), name=T, index=0), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReference(String::<!CALLABLE_REFERENCE_TO_CONTEXTUAL_DECLARATION!>test<!>, true, true)<!>
    }
}