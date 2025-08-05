package export

<!EXPORTED_MARKER_Classifier(packageName=[export], classNames=[MyClass])!>@ExportSymbol
annotation class MyClass<!>

@ChildrenExported
annotation class NotExported(
<!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[NotExported]), name=value)!>@property:ExportSymbol
val value: String<!>)

