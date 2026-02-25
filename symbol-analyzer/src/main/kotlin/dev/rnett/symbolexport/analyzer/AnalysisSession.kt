package dev.rnett.symbolexport.analyzer

import dev.rnett.symbolexport.internal.AnalysisArguments
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSdkModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.platform.CommonPlatforms
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name

@OptIn(KaExperimentalApi::class)
class AnalysisSession(
    val arguments: AnalysisArguments
) : AutoCloseable {
    companion object {
        private val logger = LoggerFactory.getLogger(AnalysisSession::class.java)
    }

    private val disposable = com.intellij.openapi.util.Disposer.newDisposable("SymbolAnalyzer.project")
    val module: KaSourceModule

    override fun close() {
        com.intellij.openapi.util.Disposer.dispose(disposable)
    }

    fun analyze(analyzerFactory: (arguments: AnalysisArguments) -> Analyzer) {
        val analyzer = analyzerFactory(arguments)
        try {
            analyzer.doAnalysis(module)
        } catch (e: Exception) {
            logger.error("Error during analysis of ${arguments.sourceSetName} with ${analyzer::class}", e)
        }
    }

    init {
        var builtModule: KaSourceModule? = null
        buildStandaloneAnalysisAPISession(disposable) {
            buildKtModuleProvider {
                fun getPlatform(platformStr: String?): TargetPlatform {
                    return when (platformStr?.lowercase()) {
                        "jvm" -> JvmPlatforms.defaultJvmPlatform
                        "common" -> CommonPlatforms.defaultCommonPlatform
                        else -> JvmPlatforms.defaultJvmPlatform
                    }
                }

                this.platform = getPlatform(arguments.targetName)

                val sdkModules = mutableMapOf<Path, org.jetbrains.kotlin.analysis.api.projectStructure.KaModule>()
                val libraryModules = mutableMapOf<Path, org.jetbrains.kotlin.analysis.api.projectStructure.KaModule>()

                val platform = getPlatform(arguments.targetName)
                builtModule = buildKtSourceModule {
                    this.moduleName = arguments.sourceSetName
                    this.platform = platform
                    addSourceRoots(arguments.sources.map { Path(it) })

                    // Add SDK if specified
                    arguments.jdkHome?.let { jdkHomeStr ->
                        val jdkHome = Path(jdkHomeStr)
                        val sdk = sdkModules.getOrPut(jdkHome) {
                            buildKtSdkModule {
                                addBinaryRootsFromJdkHome(jdkHome, isJre = false)
                                this.platform = platform
                                libraryName = "jdk-${arguments.sourceSetName}"
                            }
                        }
                        addRegularDependency(sdk)
                    }

                    // Add libraries
                    arguments.classpath.forEach { cp ->
                        val path = Path(cp)
                        val lib = libraryModules.getOrPut(path) {
                            buildKtLibraryModule {
                                addBinaryRoot(path)
                                this.platform = platform
                                libraryName = "lib-${path.name}"
                            }
                        }
                        addRegularDependency(lib)
                    }
                }
                addModule(builtModule)
            }
        }
        module = builtModule ?: error("Module was not built")
    }
}