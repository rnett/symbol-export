package dev.rnett.symbolexport.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

public object InternalNameSerializer {
    private val json: Json = Json {
        isLenient = true
        useArrayPolymorphism = true
    }

    public fun <T> serialize(entry: T, serializer: KSerializer<T>): String = json.encodeToString(serializer, entry)
    public fun <T> deserialize(data: String, serializer: KSerializer<T>): T = json.decodeFromString(serializer, data)

    public inline fun <reified T> serialize(entry: T): String = serialize(entry, serializer())
    public inline fun <reified T> deserialize(data: String): T = deserialize(data, serializer())
}