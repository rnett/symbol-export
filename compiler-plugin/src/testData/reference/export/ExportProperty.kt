package reference.export

import kotlin.math.sign

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_TopLevelMember(packageName=[kotlin, math], name=sign), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReference(Int::sign)<!>
    }
}