package errors

<!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
private val foo = 1<!>

<!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
private fun bar() = 2<!>

<!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
private class Baz<!>

@ChildrenExported
class Test {
    <!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
    private val baz = 3<!>
}
