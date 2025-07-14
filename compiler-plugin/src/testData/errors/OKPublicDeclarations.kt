package errors

<!EXPORTED_MARKER_TopLevelMember(packageName=[errors], name=foo)!>@ExportSymbol
public val foo = 1<!>

<!EXPORTED_MARKER_TopLevelMember(packageName=[errors], name=bar)!>@ExportSymbol
public fun bar() = 2<!>

<!EXPORTED_MARKER_Classifier(packageName=[errors], classNames=[Baz])!>@ExportSymbol
public class Baz<!>

@ChildrenExported
class Test {
    <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[errors], classNames=[Test]), name=baz)!>@ExportSymbol
    public val baz = 3<!>
}
