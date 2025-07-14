package export

@ChildrenExported
fun <<!EXPORTED_MARKER_TypeParameter(owner=TopLevelMember(packageName=[export], name=test), name=T, index=0)!>@ExportSymbol T<!>> test(t: T){}


@ChildrenExported
val <<!EXPORTED_MARKER_TypeParameter(owner=TopLevelMember(packageName=[export], name=test), name=T, index=0)!>@ExportSymbol T<!>> T.test: Int get() = 2

@ChildrenExported
class Test<<!EXPORTED_MARKER_TypeParameter(owner=Classifier(packageName=[export], classNames=[Test]), name=T, index=0)!>@ExportSymbol T<!>>()


