package export

@ChildrenExported
class MyClass() {
    <!EXPORTED_MARKER_Constructor(classifier=Classifier(packageName=[export], classNames=[MyClass]), name=<init>)!>@ExportSymbol
    constructor(a: Int): this()<!>

    <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[MyClass]), name=test)!>@ExportSymbol
    <!NON_ABSTRACT_FUNCTION_WITH_NO_BODY!>fun test()<!><!>

    <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[MyClass]), name=prop)!>@ExportSymbol
    val prop: Int = 0<!>

    constructor(s: String): this()

    fun notExported(){}

    val notExported: Int = 0
}
