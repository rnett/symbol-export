package export


@ChildrenExported
@ExportReceivers(extension = false)
context(s: String)
fun Int.test2(a: Int){}

@ChildrenExported
class ExtensionHelper {
    <!EXPORTED_MARKER_ReceiverParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=test), name=<this>, index=0, type=DISPATCH)!>@ChildrenExported
    context(s: String)
    @ExportReceivers(extension = false)
    fun Int.test(a: Int){}<!>

    <!EXPORTED_MARKER_ReceiverParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=test2), name=<this>, index=0, type=DISPATCH)!>@ChildrenExported
    @ExportReceivers()
    context(s: String)
    fun test2(a: Int){}<!>

    @ChildrenExported
    @ExportReceivers(extension = false, dispatch = false)
    context(s: String)
    fun Int.test3(a: Int){}
}