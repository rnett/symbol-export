// GENERATED FROM class compilertest.cases.Cases, DO NOT EDIT

@TestAnnotation(
    stringProperty = "compilertest",
    intProperty = 3,
    enumProperty = TestEnum.A,
    classProperty = FooClass::class,
    arrayProperty = ["compilertest", "test2"],
    annotationProperty = TestAnnotation.TestChildAnnotation(
        test = "test-child",
        cls = BarClass::class
    ),
    annotationArrayProperty = [
        TestAnnotation.TestChildAnnotation(
            test = "test-child-2",
            cls = BarClass::class
        ),
        TestAnnotation.TestChildAnnotation(
            test = "test-child-3",
            cls = BarClass::class
        )
    ]
)
class irRead


fun box(): String {
    return "OK"
}

