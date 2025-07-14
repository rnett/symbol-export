package export

@ChildrenExported
fun test(a: Int, <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=test), name=b, index=1, indexInList=1, type=VALUE)!>@ExportSymbol b: String<!>){}

@ChildrenExported
class Test @ChildrenExported constructor(<!EXPORTED_MARKER_ClassifierMember(classifier=Classifier(packageName=[export], classNames=[Test]), name=a), EXPORTED_MARKER_IndexedParameter(owner=Constructor(classifier=Classifier(packageName=[export], classNames=[Test]), name=<init>), name=a, index=0, indexInList=0, type=VALUE)!>@param:ExportSymbol val a: Int<!>, var b: String)

@ChildrenExported
fun Int.testWithExtensionReceiver(a: Int, <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiver), name=b, index=2, indexInList=1, type=VALUE)!>@ExportSymbol b: String<!>){}

@ChildrenExported
context(c: Double)
fun Int.testWithExtensionReceiverAndContext(a: Int, <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiverAndContext), name=b, index=3, indexInList=1, type=VALUE)!>@ExportSymbol b: String<!>){}

@ChildrenExported
class ExtensionHelper {
    @ChildrenExported
    fun Int.testWithExtensionReceiver(a: Int, <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithExtensionReceiver), name=b, index=3, indexInList=1, type=VALUE)!>@ExportSymbol b: String<!>){}

    @ChildrenExported
    fun testWithDispatchReceiver(a: Int, <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithDispatchReceiver), name=b, index=2, indexInList=1, type=VALUE)!>@ExportSymbol b: String<!>){}

    @ChildrenExported
    context(c: Double)
    fun Int.testWithExtensionReceiverAndContext(a: Int, <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithExtensionReceiverAndContext), name=b, index=4, indexInList=1, type=VALUE)!>@ExportSymbol b: String<!>){}

    @ChildrenExported
    context(c: Double)
    fun testWithDispatchReceiverAndContext(a: Int, <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithDispatchReceiverAndContext), name=b, index=3, indexInList=1, type=VALUE)!>@ExportSymbol b: String<!>){}
}

