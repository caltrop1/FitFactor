package com.sami.setra.data.importexport

import com.sami.setra.data.importexport.migrator.InvalidSchemaFormatException
import com.sami.setra.data.importexport.migrator.RoutineSchemaMigrator
import com.sami.setra.data.importexport.migrator.UnsupportedSchemaVersionException
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineSchemaMigratorTest {

    @Test
    fun `valid current version json passes through migrator`() {
        val json = """
            {
              "format": "fitfactor-routine",
              "formatVersion": 1,
              "metadata": {
                "name": "Push Pull Legs"
              }
            }
        """.trimIndent()

        val migrated = RoutineSchemaMigrator.migrateIfNeeded(json)
        assertEquals(json, migrated)
    }

    @Test(expected = InvalidSchemaFormatException::class)
    fun `empty input throws InvalidSchemaFormatException`() {
        RoutineSchemaMigrator.migrateIfNeeded("")
    }

    @Test(expected = InvalidSchemaFormatException::class)
    fun `invalid json format throws InvalidSchemaFormatException`() {
        RoutineSchemaMigrator.migrateIfNeeded("not a json string")
    }

    @Test(expected = InvalidSchemaFormatException::class)
    fun `wrong format identifier throws InvalidSchemaFormatException`() {
        val json = """
            {
              "format": "other-app-format",
              "formatVersion": 1,
              "metadata": { "name": "Test" }
            }
        """.trimIndent()

        RoutineSchemaMigrator.migrateIfNeeded(json)
    }

    @Test(expected = UnsupportedSchemaVersionException::class)
    fun `future format version throws UnsupportedSchemaVersionException`() {
        val json = """
            {
              "format": "fitfactor-routine",
              "formatVersion": 2,
              "metadata": { "name": "Future Schema" }
            }
        """.trimIndent()

        RoutineSchemaMigrator.migrateIfNeeded(json)
    }
}
