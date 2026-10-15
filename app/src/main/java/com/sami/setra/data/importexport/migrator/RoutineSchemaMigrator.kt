package com.sami.setra.data.importexport.migrator

import com.sami.setra.data.importexport.model.PortableRoutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull

class UnsupportedSchemaVersionException(val version: Int) :
    Exception("Unsupported schema version $version. Maximum supported version is ${PortableRoutine.CURRENT_SCHEMA_VERSION}.")

class InvalidSchemaFormatException(message: String) : Exception(message)

object RoutineSchemaMigrator {

    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun migrateIfNeeded(rawJsonString: String): String {
        val trimmed = rawJsonString.trim()
        if (trimmed.isEmpty()) {
            throw InvalidSchemaFormatException("The file content is empty.")
        }

        val jsonElement = try {
            jsonParser.parseToJsonElement(trimmed)
        } catch (e: Exception) {
            throw InvalidSchemaFormatException("Invalid JSON format: ${e.message}")
        }

        val jsonObject = jsonElement.jsonObject
        val format = jsonObject["format"]?.jsonPrimitive?.content
        if (format != PortableRoutine.FORMAT_IDENTIFIER) {
            throw InvalidSchemaFormatException("Unrecognized format identifier '$format'. Expected '${PortableRoutine.FORMAT_IDENTIFIER}'.")
        }

        val formatVersion = jsonObject["formatVersion"]?.jsonPrimitive?.intOrNull
            ?: throw InvalidSchemaFormatException("Missing or malformed 'formatVersion' field.")

        if (formatVersion > PortableRoutine.CURRENT_SCHEMA_VERSION) {
            throw UnsupportedSchemaVersionException(formatVersion)
        }

        if (formatVersion < 1) {
            throw InvalidSchemaFormatException("Invalid schema version $formatVersion.")
        }

        // Schema version 1 is currently the latest version.
        // If future schema versions (e.g. v2) need to be migrated to v1/current, add migration logic here.
        return trimmed
    }
}
