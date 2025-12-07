[![Maven Central Version](https://img.shields.io/maven-central/v/dev.rnett.symbol-export/symbols?style=for-the-badge)](https://central.sonatype.com/artifact/dev.rnett.symbol-export/symbols)
![Maven snapshots](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fdev%2Frnett%2Fsymbol-export%2Fgradle-plugins%2Fmaven-metadata.xml&strategy=latestProperty&style=for-the-badge&label=SNAPSHOT&color=yellow)
[![Documentation](https://img.shields.io/badge/documentation-symbol--export.rnett.dev-blue?style=for-the-badge&link=https%3A%2F%2Fsymbol-export.rnett.dev%2F)](https://symbol-export.rnett.dev)
[![GitHub License](https://img.shields.io/github/license/rnett/symbol-export?style=for-the-badge)](./LICENSE)

# Symbol-export

Symbol-export is a tool for exporting symbols from Kotlin libraries so that they can be referenced by tooling such as compiler plugins or annotation processors.

Features:

- Compile time errors if symbol names change
- Integrations with the Kotlin compiler, KSP, and Kotlinpoet
- Reading and writing of annotation instances, including their arguments, from compiler plugins or annotation processors

> [!WARNING]
> Compatibility of compiler plugins with Kotlin's Incremental Compilation is somewhat iffy and may cause issues in some scenarios.

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

## Documentation

Documentation can be found at [symbol-export.rnett.dev](https://symbol-export.rnett.dev).
