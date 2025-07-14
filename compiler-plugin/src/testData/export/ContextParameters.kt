package export

@ChildrenExported
context(s: String, <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=test), name=c, index=1, indexInList=1, type=CONTEXT)!>@ExportSymbol c: Double<!>)
fun test(a: Int){}

@ChildrenExported
context(<!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiver), name=c, index=0, indexInList=0, type=CONTEXT)!>@ExportSymbol c: Double<!>)
fun Int.testWithExtensionReceiver(a: Int){}

@ChildrenExported
class ExtensionHelper {
    @ChildrenExported
    context(<!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithExtensionReceiverAndContext), name=c, index=1, indexInList=0, type=CONTEXT)!>@ExportSymbol c: Double<!>)
    fun Int.testWithExtensionReceiverAndContext(a: Int){}

    @ChildrenExported
    context(<!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithDispatchReceiverAndContext), name=c, index=1, indexInList=0, type=CONTEXT)!>@ExportSymbol c: Double<!>)
    fun testWithDispatchReceiverAndContext(a: Int){}
}


@ChildrenExported
context(s: String, <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=test), name=c, index=1, indexInList=1, type=CONTEXT)!>@ExportSymbol c: Double<!>)
val test: Int get() = 1

@ChildrenExported
context(<!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=test), name=c, index=0, indexInList=0, type=CONTEXT)!>@ExportSymbol c: Double<!>)
val Int.test: Int get() = 1
