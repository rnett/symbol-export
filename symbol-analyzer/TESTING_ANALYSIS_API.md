# Testing Kotlin Analysis API Standalone

This document describes the recommended approach for testing Kotlin Analysis API standalone applications, based on the implementation in the `symbol-analyzer` module.

## Core Principles

1. **Use Real Standalone Sessions**: Testing against a real `KaSession` created via `buildStandaloneAnalysisAPISession` ensures that resolution, diagnostics, and symbol retrieval behave exactly as they would in production.
2. **Data-Driven Tests**: Use Kotlin source strings and compare the results against expected structures (e.g., exported JSON).
3. **Real File System**: Use `@TempDir` to write source files to disk. The Analysis API standalone mode works best when it can read files from the file system.
4. **Automatic Dependency Resolution**: For tests, it is often sufficient to use the current JVM's classpath and `java.home` as the dependencies for the analysis session.

## Test Infrastructure

### AbstractSymbolAnalyzerTest

The `AbstractSymbolAnalyzerTest` provides a foundation for analysis tests:

- **TempDir**: Uses JUnit 5's `@TempDir` for isolated test environments.
- **runAnalysis**: A helper method that:
    - Writes source code to a temporary file.
    - Collects the current classpath and JDK home.
    - Constructs `AnalysisArguments`.
    - Initializes and runs an `AnalysisSession`.
    - Decodes and returns the resulting JSON output.

### SymbolExportAnalyzerTest

Contains concrete test cases for different Kotlin constructs.

## Implementation Details

### Standalone Session Setup

Ensure that the `KaModuleProvider` is initialized with a platform:

```kotlin
buildKtModuleProvider {
    this.platform = JvmPlatforms.defaultJvmPlatform // MUST be set
    // ... add modules
}
```

### Classpath Management

In tests, you can use the current process's classpath to provide dependencies for the code being analyzed:

```kotlin
val classpath = System.getProperty("java.class.path")
    .split(File.pathSeparator)
    .filter { it.endsWith(".jar") || File(it).isDirectory }
```

## Best Practices

- **Minimize Scope**: Test one feature at a time (e.g., classes, then functions, then properties).
- **Verify Structure, Not Just Presence**: Check that the exported symbols have the correct names, packages, and types.
- **Test Negative Scenarios**: Ensure that symbols NOT marked for export are indeed NOT exported.
- **Handle Disposal**: Always use `.use { ... }` or `Disposer.dispose()` to ensure that the heavy Analysis API session is properly closed, especially in tests where many sessions might be created.
