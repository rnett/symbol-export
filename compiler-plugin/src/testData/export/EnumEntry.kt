package export

@ChildrenExported
enum class TestEnum {
    Entry1,
    <!EXPORTED_MARKER_EnumEntry(owner=Classifier(packageName=[export], classNames=[TestEnum]), name=Entry2, ordinal=1)!>@ExportSymbol Entry2;<!>
}