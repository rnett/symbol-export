package export

<!EXPORTED_MARKER_TopLevelMember(packageName=[export], name=test)!>@ExportSymbol
fun test() {}<!>

<!EXPORTED_MARKER_TopLevelMember(packageName=[export], name=prop)!>@ExportSymbol
val prop = 0<!>

fun notExported(){}

val notExportedProp: Int get() = 0