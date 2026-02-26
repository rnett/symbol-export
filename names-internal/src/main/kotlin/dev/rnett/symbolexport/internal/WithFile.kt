package dev.rnett.symbolexport.internal

import kotlinx.serialization.Serializable

@Serializable
public data class WithFile<T>(val file: String, val data: T)