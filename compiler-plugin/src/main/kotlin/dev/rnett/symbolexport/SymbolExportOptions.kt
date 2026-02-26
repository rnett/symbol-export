package dev.rnett.symbolexport

import dev.rnett.kcp.development.options.CompilerOptionsHost
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * kcp-development options host for the symbol-export compiler plugin.
 * Keeps option names and semantics identical to the previous manual implementation.
 */
object SymbolExportOptions : CompilerOptionsHost() {

    val outputDir by singular<Path>(
        name = "outputDir",
        valueDescription = "<path>",
        description = "The file to output the exported symbols to",
        required = true,
    ) { runCatching { Path(it) }.getOrNull() }

    val rootDir by singular<Path>(
        name = "rootDir",
        valueDescription = "<path>",
        description = "The root directory of the gradle project",
        required = true,
    ) { runCatching { Path(it) }.getOrNull() }

    val projectName by singularString(
        name = "projectName",
        valueDescription = "<string>",
        description = "The configured name of the gradle project",
        required = true,
    )

    val projectGroup by singularString(
        name = "projectGroup",
        valueDescription = "<string>",
        description = "The group of the gradle project",
        required = true,
    )

    val projectArtifact by singularString(
        name = "projectArtifact",
        valueDescription = "<string>",
        description = "The artifact of the gradle project",
        required = true,
    )

    val projectVersion by singularString(
        name = "projectVersion",
        valueDescription = "<string>",
        description = "The gradle project version",
        required = true,
    )

    // Previously not marked required on the CLI but used as required by the registrar.
    // Keep CLI semantics (not required flag) while registrar will still enforce presence.
    val sourceSetName by singularString(
        name = "sourceSetName",
        valueDescription = "<string>",
        description = "The source set name",
        required = false,
    )

    // Previously optional with default false
    val warnOnExported by flag(
        name = "warnOnExported",
        defaultValue = false,
        description = "Whether to add a warning diagnostic on exported symbols",
    )
}
