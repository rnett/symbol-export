package dev.rnett.symbolexport.analyzer

import dev.rnett.symbolexport.internal.AnalysisArguments
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.system.exitProcess


object SymbolAnalyzer {
    private val logger = LoggerFactory.getLogger(SymbolAnalyzer::class.java)

    @JvmStatic
    @OptIn(KaExperimentalApi::class)
    fun main(args: Array<String>) {
        try {
            val inputArgs = try {
                if (args.isEmpty()) {
                    logger.error("No arguments provided")
                    exitProcess(1)
                }
                AnalysisArguments.deserialize(Path(args[0]).readText())

            } catch (e: Throwable) {
                logger.error("Couldn't parse arguments {}", args.contentToString(), e)
                exitProcess(1)
            }

            logger.info("Creating Analysis API session for project {}...", inputArgs.projectName)
            val analysisSession = try {
                AnalysisSession(inputArgs)
            } catch (t: Throwable) {
                logger.error("Couldn't create analyzer session", t)
                exitProcess(1)
            }

            val outputDir = Path(inputArgs.outputDir)
            outputDir.createDirectories()

            analysisSession.use { session ->
                session.analyze { args ->
                    SymbolExportAnalyzer(
                        args.projectName,
                        args.projectCoordinates,
                        args.sourceSetName,
                        outputDir
                    )
                }
            }
            exitProcess(0)

        } catch (e: Throwable) {
            logger.error("Unexpected uncaught error", e)
            exitProcess(1)
        }
    }
}
