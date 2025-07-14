package errors

<!EXPORTED_MARKER_TopLevelMember(packageName=[errors], name=foo)!>@ExportSymbol
@PublishedApi internal val foo = 1<!>

<!EXPORTED_MARKER_TopLevelMember(packageName=[errors], name=bar)!>@ExportSymbol
@PublishedApi internal fun bar() = 2<!>

<!EXPORTED_MARKER_Classifier(packageName=[errors], classNames=[Baz])!>@ExportSymbol
@PublishedApi internal class Baz<!>

@ChildrenExported
class Test {
    <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[errors], classNames=[Test]), name=baz)!>@ExportSymbol
    @PublishedApi internal val baz = 3<!>
}
