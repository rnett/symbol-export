package dev.rnett.symbolexport.internal

import kotlinx.serialization.Serializable


@Serializable
public data class ProjectCoordinates(val group: String, val artifact: String, val version: String)
