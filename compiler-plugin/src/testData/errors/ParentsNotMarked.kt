package errors

<!SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED!>class Test {
    @ExportSymbol
    fun test(){}
}<!>

// OK
@ChildrenExported
class Test2 {
    <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[errors], classNames=[Test2]), name=test)!>@ExportSymbol
    fun test(){}<!>

    // Not OK

    <!SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED!>class Nested1 {
        @ExportSymbol
        fun test2(){}
    }<!>

    // OK
    @ChildrenExported
    class Nested2 {
        <!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[errors], classNames=[Test2, Nested2]), name=test2)!>@ExportSymbol
        fun test2(){}<!>
    }
}


<!SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED!>fun test(@ExportSymbol v: Int){}<!>


<!SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED!>fun <@ExportSymbol T> test2(v: Int){}<!>

<!SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED!>context(@ExportSymbol v: Int<!SYNTAX!><!>
fun test(){}<!>

<!SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED!>fun @receiver:ExportSymbol Int.test(){}<!>

<!SYMBOL_EXPORT_PARENT_MUST_BE_EXPOSED!>enum class TestEnum {
    @ExportSymbol
    Entry1,
    Entry2
}<!>

