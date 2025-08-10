import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("build.kotlin-jvm")
    id("build.publishing")
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.shadow)
    `java-test-fixtures`
    idea
}

val compilerTestRuntimeClasspath by configurations.registering {
    isCanBeResolved = true
    isTransitive = true
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("compiler"))

    implementation(project(":names-internal"))

    compilerTestRuntimeClasspath(project(":annotations"))

    testFixturesApi(kotlin("test-junit5"))
    testFixturesApi(kotlin("compiler-internal-test-framework"))
    testFixturesApi(kotlin("compiler"))

    // Dependencies required to run the internal test framework.
    testRuntimeOnly(kotlin("script-runtime"))
    testRuntimeOnly(kotlin("annotations-jvm"))
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

tasks.shadowJar {
    archiveClassifier = ""
    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
    }
    relocate("kotlinx.serialization", "dev.rnett.symbolexport.kotlinx.serialization")
}

buildConfig {
    useKotlinOutput {
        internalVisibility = true
    }

    packageName("dev.rnett.symbolexport")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")
}

kotlin {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    explicitApi = ExplicitApiMode.Disabled
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation { enabled = false }
}

tasks.test {
    dependsOn(compilerTestRuntimeClasspath)
    maxHeapSize = "2g"
    workingDir = projectDir

    systemProperty("compilerTestRuntime.classpath", compilerTestRuntimeClasspath.map { it.asPath }.get())

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
    mainClass.set("dev.rnett.lattice.GenerateTestsKt")
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