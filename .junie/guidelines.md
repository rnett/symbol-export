
## Project structure

- `annotations` contains annotations that are shared between the compiler plugin and the runtime
- `compiler-plugin` is the Kotlin compiler plugin
- `gradle-plugin` is the Gradle plugin that applies the compiler plugin

## Kotlin compiler plugins

General compiler plugin instructions can be found in the links in the
readme [here](https://github.com/bnorm/buildable/blob/main/README.md).
Instructions on testing Kotlin compiler plugins can be
found [here](https://github.com/JetBrains/kotlin/blob/master/compiler/test-infrastructure/ReadMe.md).

## Misc

- Only run the JVM tests, ignore other platforms
- Run the common tests on the JVM target to test them. Do not run them on other targets.