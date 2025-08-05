package export

import kotlin.reflect.KClass

enum class TestEnum { A, B }

<!EXPORTED_MARKER_Annotation(packageName=[export], classNames=[MyAnnotation], parameters={s=STRING, a=Array(elementType=INT), cls=KClass, enu=Enum(enumClass=Classifier(packageName=[export], classNames=[TestEnum])), ann2=Annotation(annotationClass=Classifier(packageName=[export], classNames=[MyAnnotation2]))})!>@ExportAnnotation
annotation class MyAnnotation(
    val s: String,
    val a: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Array<Int><!>,
    val cls: KClass<*>,
    val enu: TestEnum,
    val ann2: MyAnnotation2,
)<!>

<!EXPORTED_MARKER_Annotation(packageName=[export], classNames=[MyAnnotation2], parameters={a=LONG, t=Array(elementType=KClass)})!>@ExportAnnotation
annotation class MyAnnotation2(val a: Long, val t: Array<KClass<*>>)<!>

