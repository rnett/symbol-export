package dev.rnett.symbolexport.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.opentest4j.AssertionFailedError
import org.opentest4j.FileInfo
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertTrue

abstract class AbstractGeneratorSnapshotTest {

    private val snapshotsDir: Path = Path("src/test/resources/snapshots")

    private fun verifyCompilation(code: String, snapshotName: String) {
        val compiler = K2JVMCompiler()
        val tempDir = Files.createTempDirectory("kotlin-compilation-${snapshotName.replace("/", "_").replace("\\", "_")}")
        val sourceFile = tempDir.resolve("Source.kt")
        sourceFile.writeText(code)

        val args = compiler.createArguments().apply {
            freeArgs = listOf(sourceFile.absolutePathString())
            classpath = System.getProperty("java.class.path")
            destination = tempDir.resolve("out").absolutePathString()
            noStdlib = true
            optIn = arrayOf("dev.rnett.symbolexport.symbol.SymbolExportInternals")
        }

        val messageCollector = PrintingMessageCollector(System.out, MessageRenderer.WITHOUT_PATHS, false)
        val result = compiler.exec(messageCollector, Services.EMPTY, args)

        if (result != ExitCode.OK) {
            throw AssertionFailedError("Generated code for $snapshotName failed to compile (exit code: $result). See stdout for errors.")
        }
    }

    private fun assertSnapshot(actual: String, snapshotName: String) {
        val snapshotFile = snapshotsDir.resolve("$snapshotName.kt")
        val updateSnapshots = System.getProperty("updateSnapshots")?.toBoolean() ?: false

        val actualContent = actual.trim().replace("\r\n", "\n")

        if (updateSnapshots) {
            snapshotFile.createParentDirectories()
            snapshotFile.writeText(actualContent)
            verifyCompilation(actualContent, snapshotName)
            return
        }

        assertTrue(snapshotFile.exists(), "Snapshot file for $snapshotName does not exist: $snapshotFile")

        val expected = if (snapshotFile.exists()) snapshotFile.readText() else ""

        val expectedContent = expected.trim().replace("\r\n", "\n")
        if (expectedContent != actualContent) {
            throw AssertionFailedError(
                "Generated code does not match snapshot: $snapshotName",
                FileInfo(snapshotFile.absolutePathString(), expected.toByteArray(StandardCharsets.UTF_8)),
                actual,
            )
        }
        verifyCompilation(actualContent, snapshotName)
    }

    protected fun assertSnapshot(fileSpec: FileSpec, snapshotName: String) {
        assertSnapshot(fileSpec.toString(), snapshotName)
    }

    val parentName = ClassName("com.example", "Symbols")

    protected fun assertSnapshot(typeSpec: TypeSpec, snapshotName: String) {
        val fileSpec = FileSpec.builder("com.example", "Symbols")
            .addType(
                TypeSpec.objectBuilder("Symbols")
                    .addType(typeSpec)
                    .build()
            )
            .build()
        assertSnapshot(fileSpec, snapshotName)
    }

    protected fun assertCodeBlockSnapshot(codeBlock: CodeBlock, snapshotName: String) {
        val fn = FunSpec.builder("wrapper").returns(Unit::class)
        fn.addCode(codeBlock)
        val type = TypeSpec.objectBuilder("Generated")
        type.addFunction(fn.build())
        assertSnapshot(type.build(), snapshotName)
    }
}
