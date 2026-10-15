package com.sami.setra.data.importexport

import com.sami.setra.data.importexport.model.PortableDay
import com.sami.setra.data.importexport.model.PortableMetadata
import com.sami.setra.data.importexport.model.PortableRoutine
import com.sami.setra.data.importexport.validator.RoutineSchemaValidator
import com.sami.setra.data.importexport.validator.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineSchemaValidatorTest {

    @Test
    fun `valid routine returns Valid`() {
        val routine = PortableRoutine(
            format = "fitfactor-routine",
            formatVersion = 1,
            metadata = PortableMetadata(name = "Valid Routine"),
            days = listOf(
                PortableDay(dayOfWeek = 1),
                PortableDay(dayOfWeek = 3),
                PortableDay(dayOfWeek = 5)
            )
        )

        val result = RoutineSchemaValidator.validate(routine)
        assertEquals(ValidationResult.Valid, result)
    }

    @Test
    fun `invalid format identifier returns Invalid`() {
        val routine = PortableRoutine(
            format = "unknown-format",
            formatVersion = 1,
            metadata = PortableMetadata(name = "Routine")
        )

        val result = RoutineSchemaValidator.validate(routine)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("Invalid format identifier"))
    }

    @Test
    fun `unsupported schema version returns Invalid`() {
        val routine = PortableRoutine(
            format = "fitfactor-routine",
            formatVersion = 99,
            metadata = PortableMetadata(name = "Future Routine")
        )

        val result = RoutineSchemaValidator.validate(routine)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("Unsupported format version"))
    }

    @Test
    fun `blank routine name returns Invalid`() {
        val routine = PortableRoutine(
            format = "fitfactor-routine",
            formatVersion = 1,
            metadata = PortableMetadata(name = "   ")
        )

        val result = RoutineSchemaValidator.validate(routine)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("Routine name cannot be blank"))
    }

    @Test
    fun `invalid day of week returns Invalid`() {
        val routine = PortableRoutine(
            format = "fitfactor-routine",
            formatVersion = 1,
            metadata = PortableMetadata(name = "Invalid Day Routine"),
            days = listOf(
                PortableDay(dayOfWeek = 8) // Valid days are 1..7
            )
        )

        val result = RoutineSchemaValidator.validate(routine)
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).reason.contains("Invalid day of week"))
    }
}
