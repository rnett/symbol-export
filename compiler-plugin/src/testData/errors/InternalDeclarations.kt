package errors

<!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
internal val foo = 1<!>

<!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
internal fun bar() = 2<!>

<!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
internal class Baz<!>

@ChildrenExported
class Test {
    <!SYMBOL_EXPORT_MUST_BE_PUBLIC_OR_PUBLISHED_API!>@ExportSymbol
    internal val baz = 3<!>
}
