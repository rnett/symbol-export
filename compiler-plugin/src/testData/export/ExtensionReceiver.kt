package export

@ChildrenExported
context(s: String)
fun <!EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[export], name=test), name=<receiver>, index=1, type=EXTENSION)!>@receiver:ExportSymbol Int<!>.test(a: Int){}

@ChildrenExported
@ExportReceivers
context(s: String)
fun <!EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[export], name=test2), name=<receiver>, index=1, type=EXTENSION)!>Int<!>.test2(a: Int){}

@ChildrenExported
class ExtensionHelper {
    @ChildrenExported
    context(s: String)
    fun <!EXPORTED_MARKER_ReceiverParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=test), name=<receiver>, index=2, type=EXTENSION)!>@receiver:ExportSymbol Int<!>.test(a: Int){}

    @ChildrenExported
    @ExportReceivers(dispatch = false)
    context(s: String)
    fun <!EXPORTED_MARKER_ReceiverParameter(owner=ClassifierMember(classifier=Classifier(packageName=[export], classNames=[ExtensionHelper]), name=test2), name=<receiver>, index=2, type=EXTENSION)!>Int<!>.test2(a: Int){}

    @ChildrenExported
    @ExportReceivers(extension = false, dispatch = false)
    context(s: String)
    fun Int.test3(a: Int){}
}


@ChildrenExported
context(s: String)
val <!EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[export], name=test), name=<receiver>, index=1, type=EXTENSION)!>@receiver:ExportSymbol Int<!>.test: Int get() = 1

@ChildrenExported
@ExportReceivers(dispatch = false)
context(s: String)
val <!EXPORTED_MARKER_ReceiverParameter(owner=TopLevelMember(packageName=[export], name=test2), name=<receiver>, index=1, type=EXTENSION)!>Int<!>.test2: Int get() = 1

