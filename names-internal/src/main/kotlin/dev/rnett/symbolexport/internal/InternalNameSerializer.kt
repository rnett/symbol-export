package dev.rnett.symbolexport.internal

import kotlinx.serialization.json.Json

public object InternalNameSerializer {
    internal val json: Json = Json {
        isLenient = true
        useArrayPolymorphism = true
        ignoreUnknownKeys = true
    }

    public fun serializeEntry(entry: InternalNameEntry): String = json.encodeToString(entry)
    public fun deserializeEntry(json: String): InternalNameEntry = this.json.decodeFromString(json)

}