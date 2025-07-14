package errors

fun test(){
    <!SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS!><!WRONG_ANNOTATION_TARGET!>@ExportSymbol<!>
    val foo = 1<!>

    <!SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS!>@ExportSymbol
    fun bar() = 2<!>

    <!SYMBOL_EXPORT_NO_LOCAL_DECLARATIONS!>@ExportSymbol
    class Baz<!>
}