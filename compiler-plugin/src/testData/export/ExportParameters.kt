package export

@ExportParameters
@ChildrenExported
fun test(
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=test), name=a, index=0, indexInList=0, type=VALUE)!>a: Int<!>,
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=test), name=b, index=1, indexInList=1, type=VALUE)!>b: String<!>
) {}

@ChildrenExported
class Test @ExportParameters @ChildrenExported constructor(
    <!EXPORTED_MARKER_IndexedParameter(owner=Constructor(classifier=Classifier(packageName=[export], classNames=[Test]), name=<init>), name=a, index=0, indexInList=0, type=VALUE)!>val a: Int<!>,
    <!EXPORTED_MARKER_IndexedParameter(owner=Constructor(classifier=Classifier(packageName=[export], classNames=[Test]), name=<init>), name=b, index=1, indexInList=1, type=VALUE)!>b: String<!>
)

@ExportParameters
@ChildrenExported
fun <!EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiver), name=<receiver>, index=0, type=EXTENSION)!>Int<!>.testWithExtensionReceiver(
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiver), name=a, index=1, indexInList=0, type=VALUE)!>a: Int<!>,
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiver), name=b, index=2, indexInList=1, type=VALUE)!>b: String<!>
) {}

@ExportParameters
@ChildrenExported
context(<!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithContext), name=c, index=0, indexInList=0, type=CONTEXT)!>c: Double<!>)
fun testWithContext(
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithContext), name=a, index=1, indexInList=0, type=VALUE)!>a: Int<!>,
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithContext), name=b, index=2, indexInList=1, type=VALUE)!>b: String<!>
) {}

@ExportParameters
@ChildrenExported
context(<!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiverAndContext), name=c, index=0, indexInList=0, type=CONTEXT)!>c: Double<!>)
fun <!EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiverAndContext), name=<receiver>, index=1, type=EXTENSION)!>Int<!>.testWithExtensionReceiverAndContext(
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiverAndContext), name=a, index=2, indexInList=0, type=VALUE)!>a: Int<!>,
    <!EXPORTED_MARKER_IndexedParameter(owner=TopLevelMember(packageName=[export], name=testWithExtensionReceiverAndContext), name=b, index=3, indexInList=1, type=VALUE)!>b: String<!>
) {}

@ChildrenExported
class ExtensionHelper {
    <!EXPORTED_MARKER_ReceiverParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithExtensionReceiver), name=<this>, index=0, type=DISPATCH)!>@ExportParameters
    @ChildrenExported
    fun <!EXPORTED_MARKER_ReceiverParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithExtensionReceiver), name=<receiver>, index=1, type=EXTENSION)!>Int<!>.testWithExtensionReceiver(
        <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithExtensionReceiver), name=a, index=2, indexInList=0, type=VALUE)!>a: Int<!>,
        <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithExtensionReceiver), name=b, index=3, indexInList=1, type=VALUE)!>b: String<!>
    ) {}<!>

    <!EXPORTED_MARKER_ReceiverParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithDispatchReceiver), name=<this>, index=0, type=DISPATCH)!>@ExportParameters
    @ChildrenExported
    fun testWithDispatchReceiver(
        <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithDispatchReceiver), name=a, index=1, indexInList=0, type=VALUE)!>a: Int<!>,
        <!EXPORTED_MARKER_IndexedParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=testWithDispatchReceiver), name=b, index=2, indexInList=1, type=VALUE)!>b: String<!>
    ) {}<!>
}
