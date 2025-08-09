package compilertest.cases

import kotlin.properties.PropertyDelegateProvider

abstract class TestCaseContainer {
    val cases = mutableListOf<TestCase>()

    fun case(
        builder: CaseBuilder.() -> Unit
    ) = PropertyDelegateProvider<TestCaseContainer, Lazy<TestCase>> { thisRef, property ->
        val case = CaseBuilder().apply(builder).build(property.name)
        thisRef.cases += case
        lazyOf(case)
    }

    fun casesFor(name: String): List<TestCase> = cases.filter { it.name == name }
}