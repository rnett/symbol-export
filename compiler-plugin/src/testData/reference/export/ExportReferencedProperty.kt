package reference.export

import kotlin.math.sign

@ExportReferences
private object References : BaseReferenceExporter() {
    init {
        <!EXPORTED_MARKER_TopLevelMember(packageName=[kotlin, math], name=sign), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReferenced(3.sign)<!>
        <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[kotlin, collections], classNames=[List]), name=size), SYMBOL_EXPORT_REFERENCE_EXPORTING!>exportReferenced(placeholder<List<Int>>().size)<!>
    }
}