package errors

<!EXPORTED_MARKER_Annotation(packageName=[errors], classNames=[TestAnnotation], parameters={}), SYMBOL_EXPORT_EXPORTING_EXPORTED_ANNOTATION!>@ExportAnnotation
@ExportSymbol
annotation class TestAnnotation<!>

<!EXPORTED_MARKER_Annotation(packageName=[errors], classNames=[TestAnnotation2], parameters={}), SYMBOL_EXPORT_EXPORTING_EXPORTED_ANNOTATION!>@ExportAnnotation
@ChildrenExported
annotation class TestAnnotation2<!>

<!EXPORTED_MARKER_Annotation(packageName=[errors], classNames=[TestAnnotation3], parameters={test=INT})!>@ExportAnnotation
annotation class TestAnnotation3(<!SYMBOL_EXPORT_EXPORTING_FROM_EXPORTED_ANNOTATION!>@property:ExportSymbol val test: Int<!>)<!>

