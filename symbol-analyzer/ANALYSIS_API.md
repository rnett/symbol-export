# Kotlin Analysis API in Symbol Export

This project uses the Kotlin Analysis API to analyze Kotlin code and export symbols. The `symbol-analyzer` module contains the analyzer that uses the Analysis API in standalone mode.

## Testing Strategy

For testing the Analysis API standalone application, we recommend a mix of custom infrastructure and, for advanced needs, JetBrains' published test framework.

### Published Libraries

The primary published library from JetBrains for testing the Analysis API is:

- **`org.jetbrains.kotlin:analysis-api-test-framework`**: This is the same framework used by the Kotlin compiler team to test the Analysis API. It is very powerful but can be complex to set up as it's designed for data-driven tests with
  many directives.

### Lessons from Industry (detekt and dokka)

Large projects like **detekt** and **dokka** currently implement their own thin wrappers around `buildStandaloneAnalysisAPISession` rather than relying on a shared testing library. This is often done to keep test execution fast and tailored
to their specific needs (e.g., virtual file systems, custom module structures).

- **dokka**: Uses a sophisticated `TestProject` DSL in their `analysis-kotlin-api` module to define virtual projects with multiple source sets and targets.
- **detekt**: Uses a streamlined `KotlinAnalysisApiEngine` that handles on-the-fly compilation of code strings for rule testing.

### Our Approach

In this project, we use `AbstractSymbolAnalyzerTest` which:

1. Manages a real `KaSession` via `buildStandaloneAnalysisAPISession`.
2. Uses `@TempDir` to provide a real file system for the Analysis API to work with.
3. Automatically resolves the current classpath and JDK to simplify dependency management in tests.

## Overview

The `SymbolAnalyzer` class is the entry point for analysis. It sets up a standalone Analysis API session, which provides access to the Kotlin compiler's internals (FIR, symbols, etc.) without requiring a full IDE environment or a compiler
plugin.

## How it Works

1. **Standalone Session**: The analyzer uses `buildStandaloneAnalysisAPISession` to initialize the Kotlin Analysis environment.
2. **Module Structure**: It builds a `KaModule` structure that includes:
    - **Source Module**: Contains the Kotlin source files to be analyzed.
    - **SDK Module**: Provides the JDK for resolution.
    - **Library Modules**: Provide the classpath (dependencies) for resolution.
3. **Analysis Session**: Within an `analyze` block, it can access `KaSymbol`s and perform various checks or extraction tasks.
4. **Disposal**: It is critical to dispose of the `projectDisposable` AND call `disposeGlobalStandaloneApplicationServices()` to ensure all resources (including coroutine threads) are finalized, otherwise the process may hang.

## SymbolAnalyzer Main Arguments

The `main` function of `SymbolAnalyzer` expects the following arguments (to be implemented):

- `--sources`: Comma-separated list of source root directories or files.
- `--classpath`: Comma-separated list of classpath entries (JARs or directories).
- `--jdk-home`: Path to the JDK home directory.
- `--module-name`: Name of the module being analyzed.
- `--output`: Path to the output file where exported symbols will be written (JSON).

## Adding Analysis Tasks

Analysis tasks are registered per source set in Gradle.

### Gradle Integration (Implementation Details)

To add an analysis task to a module:

1. **Register Task**: Create a new task of type `JavaExec` (or a custom task type that calls `SymbolAnalyzer.main`).
2. **Configure Inputs**:
    - `sources`: Use `kotlinSourceSet.kotlin.sourceDirectories`.
    - `classpath`: Use the compilation's `compileDependencyFiles`.
    - `jdkHome`: Can be obtained from the Gradle `JavaToolchainService`.
3. **Configure Outputs**:
    - Define the output JSON file.

Example:

```kotlin
val analysisTask = tasks.register<JavaExec>("analyzeSymbols") {
    mainClass.set("dev.rnett.symbolexport.analyzer.SymbolAnalyzerKt")
    classpath = configurations["symbolAnalyzerClasspath"] // Configuration containing symbol-analyzer shadow JAR

    val sources = kotlin.sourceSets["main"].kotlin.sourceDirectories
    val compileClasspath = configurations["compileClasspath"]

    args(
        "--sources", sources.joinToString(","),
        "--classpath", compileClasspath.joinToString(","),
        "--jdk-home", javaToolchains.launcherFor(java.toolchain).get().metadata.installationPath.asFile.absolutePath,
        "--module-name", project.name,
        "--output", layout.buildDirectory.file("exported-symbols.json").get().asFile.absolutePath
    )
}
```

## Implementation TODOs

- [ ] Implement command line argument parsing in `SymbolAnalyzer.main`.
- [ ] Implement actual symbol detection logic using the Analysis API.
- [ ] Export detected symbols to JSON.
