![Maven Central Version](https://img.shields.io/maven-central/v/dev.rnett.symbol-export/symbols?style=for-the-badge)
![Documentation](https://img.shields.io/badge/documentation-symbol--export.rnett.dev-blue?style=for-the-badge&link=https%3A%2F%2Fsymbol-export.rnett.dev%2F)
![GitHub License](https://img.shields.io/github/license/rnett/symbol-export?style=for-the-badge)

# Symbol-export

Symbol-export is a tool for exporting symbols from Kotlin libraries so that they can be referenced by tooling such as compile plugins or annotation processors.

Features:

- Compile time errors if symbol names change
- Integration with the Kotlin compiler, KSP, and Kotlinpoet
- Reading and writing of annotation instances

## Getting started

All you need to do is apply the Gradle plugins to the appropriate projects and add a dependency between the exported symbols and the project that uses them.

```kotlin
// exporter

plugins {
    id("dev.rnett.symbol-export.export")
}
```

```kotlin
// importer

plugins {
    id("dev.rnett.symbol-export.import")
}

dependencies {
    importSymbols(project(":exporter"))
}
```

Then the importer project can import a `Symbols` object containing the exported symbols for any declarations in `exporter` marked with `@ExportSymbol`.

## Documentation

TODO link