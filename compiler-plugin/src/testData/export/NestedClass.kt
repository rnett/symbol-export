package export

<!EXPORTED_MARKER_Classifier(packageName=[export], classNames=[ExportedClass])!>@ExportSymbol
class ExportedClass {

    <!EXPORTED_MARKER_Classifier(packageName=[export], classNames=[ExportedClass, NestedExport])!>@ExportSymbol
    class NestedExport <!EXPORTED_MARKER_Constructor(classifier=Classifier(packageName=[export], classNames=[ExportedClass, NestedExport]), name=<init>)!>@ExportSymbol constructor()<!> {
        <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExportedClass, NestedExport]), name=export)!>@ExportSymbol
        fun export(){}<!>

        <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExportedClass, NestedExport]), name=export)!>@ExportSymbol
        val export: Int = 2<!>
    }<!>

    class NestedNonExport

}<!>

@ChildrenExported
class NonExportedClass {

    <!EXPORTED_MARKER_Classifier(packageName=[export], classNames=[NonExportedClass, NestedExport])!>@ExportSymbol
    class NestedExport <!EXPORTED_MARKER_Constructor(classifier=Classifier(packageName=[export], classNames=[NonExportedClass, NestedExport]), name=<init>)!>@ExportSymbol constructor()<!> {
        <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[NonExportedClass, NestedExport]), name=export)!>@ExportSymbol
        fun export(){}<!>

        <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[NonExportedClass, NestedExport]), name=export)!>@ExportSymbol
        val export: Int = 2<!>
    }<!>

    class NestedNonExport

}
