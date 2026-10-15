package com.sami.setra.data.importexport.serializer

import com.sami.setra.data.importexport.model.PortableRoutine
import kotlinx.serialization.json.Json

object RoutineSerializer {

    val jsonInstance = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
        coerceInputValues = true
    }

    fun serialize(routine: PortableRoutine): String {
        return jsonInstance.encodeToString(PortableRoutine.serializer(), routine)
    }

    fun deserialize(jsonString: String): PortableRoutine {
        return jsonInstance.decodeFromString(PortableRoutine.serializer(), jsonString)
    }
}
