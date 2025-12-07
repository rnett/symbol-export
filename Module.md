# Symbol-export

[Symbol-export](https://github.com/rnett/symbol-export) is a tool for exporting symbols from Kotlin libraries so that they can be referenced by tooling such as compiler plugins or annotation processors.

Features:

- Compile time errors if symbol names change
- Integrations with the <a href="./symbols-kotlin-compiler/index.html">Kotlin compiler</a>, <a href="./symbols-ksp/index.html">KSP</a>, and <a href="./symbols-kotlinpoet/index.html">Kotlinpoet</a>
- Reading and writing of annotation instances, including their arguments, from compiler plugins or annotation processors

**WARNING:** Compatibility of compiler plugins with Kotlin's Incremental Compilation is somewhat iffy and may cause issues in some scenarios.

## Getting started

All you need to do is apply the Gradle plugins to the appropriate projects and add a dependency between the exported symbols and the project that uses them.

### Exporting project

##### build.gradle.kts

```kotlin
plugins {
    id("dev.rnett.symbol-export.export")
}

name = "foobar"
```

##### Your code

```kotlin
package foo.bar

@ExportSymbol
fun bar() {
}

@ExportSymbol
class FooService {
}
```

### Importing project

##### build.gradle.kts

```kotlin
plugins {
    id("dev.rnett.symbol-export.import")
}

dependencies {
    importSymbols(project(":foobar"))
}
```

##### Your code

```kotlin
val bar = Symbols.foobar.foo_bar_bar
val fooService = Symbols.foobar.foo_bar_FooService
```

By default, the `Symbols` object is generated with a package matching the importing project's group ID.

Then the importer project can import a `Symbols` object containing the exported symbols for any declarations in `exporter` marked with `@ExportSymbol` (`Symbol`'s package defaults to the project's group ID).

Dependencies on the <a href="./annotations/index.html">annotations</a> and <a href="./symbols/index.html">symbol library</a> will be added automatically to the exporting and importing projects, respectively (this can be opted out of).

## Usage

For more advanced usage, check out the available <a href="./annotations/index.html">annotations</a> and consult the configuration options of the <a href="./gradle-plugins/index.html">Gradle plugins</a>.
