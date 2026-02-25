package dev.rnett.symbolexport.internal

import kotlinx.serialization.Serializable

@Serializable
public data class AnalysisArguments(
    val projectName: String,
    val projectCoordinates: ProjectCoordinates,
    val sourceSetName: String,
    val targetName: String,
    val jdkHome: String?,
    val sources: List<String>,
    val classpath: List<String>,
    val outputDir: String,
) {


    public fun serialize(): String {
        return InternalNameSerializer.json.encodeToString<AnalysisArguments>(this)
    }

    public companion object {
        public fun deserialize(string: String): AnalysisArguments {
            return InternalNameSerializer.json.decodeFromString<AnalysisArguments>(string)
        }
    }
}