plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-test-fixtures`
    idea
    id("dev.rnett.symbol-export.import")
}

val compilerTestRuntimeClasspath: Configuration by configurations.creating {
    isCanBeResolved = true
    isTransitive = true
}

dependencies {
    implementation(kotlin("compiler"))
    implementation(kotlin("test-junit5"))

    implementation("dev.rnett.symbol-export:symbols-kotlin-compiler")

    //TODO update to new dependencies
//    importSymbols(project(":symbols-integration-tests:test-symbols"))
    compilerTestRuntimeClasspath(project(":symbols-integration-tests:test-symbols"))

    testFixturesApi(kotlin("test-junit5"))
    testFixturesApi(kotlin("compiler-internal-test-framework"))
    testFixturesApi(kotlin("compiler"))

    // Dependencies required to run the internal test framework.
    testRuntimeOnly(kotlin("script-runtime"))
    testRuntimeOnly(kotlin("annotations-jvm"))
}
kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

sourceSets {
    test {
        java.srcDir("src/test-gen")
        resources.srcDir("src/testData")
    }
}

idea {
    module.generatedSourceDirs.add(projectDir.resolve("src/test-gen"))
}

tasks.test {
    useJUnitPlatform()
    dependsOn(compilerTestRuntimeClasspath)
    maxHeapSize = "2g"
    workingDir = projectDir

    systemProperty("compilerTestRuntime.classpath", compilerTestRuntimeClasspath.asPath)

    // Properties required to run the internal test framework.
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")

    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", projectDir)
}

val generateTests by tasks.registering(JavaExec::class) {
    inputs.dir(layout.projectDirectory.dir("src/testData"))
        .withPropertyName("testData")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(layout.projectDirectory.dir("src/test-gen"))
        .withPropertyName("generatedTests")

    classpath = sourceSets.testFixtures.get().runtimeClasspath
    mainClass.set("test.GenerateTestsKt")
    workingDir = projectDir
}

val clearDumps by tasks.registering(Delete::class) {
    delete(
        fileTree(layout.projectDirectory.dir("src/testData")) {
            include("**/*.fir.txt")
            include("**/*.fir.ir.txt")
        }
    )
}

tasks.test {
    mustRunAfter(clearDumps)
}

tasks.compileTestKotlin {
    dependsOn(generateTests)
}

fun Test.setLibraryProperty(propName: String, jarName: String) {
    val path = project.configurations
        .testRuntimeClasspath.get()
        .files
        .find { """$jarName-\d.*jar""".toRegex().matches(it.name) }
        ?.absolutePath
        ?: return
    systemProperty(propName, path)
}