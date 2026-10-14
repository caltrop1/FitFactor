package com.sami.setra.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RecordType(val displayName: String) {
    HEAVIEST_WEIGHT("Max Weight"),
    MAX_REPS("Max Reps"),
    MAX_SET_VOLUME("Max Set Volume"),
    ESTIMATED_1RM("Est. 1RM"),
    MAX_DURATION("Max Duration")
}

data class PersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val recordType: RecordType,
    val value: Double,
    val unit: String,
    val associatedWeight: Double? = null,
    val associatedReps: Double? = null,
    val achievedAt: Long,
    val sessionId: Long,
    val workoutName: String
) {
    val formattedValue: String
        get() = when (recordType) {
            RecordType.HEAVIEST_WEIGHT -> "${formatValue(value)} $unit"
            RecordType.MAX_REPS -> "${value.toInt()} reps${associatedWeight?.let { " @ ${formatValue(it)} $unit" } ?: ""}"
            RecordType.MAX_SET_VOLUME -> "${formatValue(value)} $unit"
            RecordType.ESTIMATED_1RM -> "~${formatValue(value)} $unit"
            RecordType.MAX_DURATION -> "${value.toInt()}s"
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return sdf.format(Date(achievedAt))
        }

    private fun formatValue(v: Double): String {
        return if (v % 1.0 == 0.0) v.toInt().toString() else String.format(Locale.getDefault(), "%.1f", v)
    }
}

enum class TimeFrame(val label: String, val days: Int) {
    WEEK_1("7 Days", 7),
    MONTH_1("30 Days", 30),
    MONTH_3("3 Months", 90),
    YEAR_1("1 Year", 365),
    ALL("All Time", Int.MAX_VALUE)
}

data class ExerciseProgressionPoint(
    val timestamp: Long,
    val workoutName: String,
    val sessionId: Long,
    val bestWeight: Double,
    val bestReps: Double,
    val totalVolume: Double,
    val estimated1RM: Double?,
    val completedSetsCount: Int
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

data class VolumeDataPoint(
    val timestamp: Long,
    val label: String,
    val volumeKg: Double,
    val setsCount: Int,
    val workoutsCount: Int
)

data class WorkoutFrequencyStats(
    val totalCompletedWorkouts: Int,
    val workoutsThisWeek: Int,
    val workoutsThisMonth: Int,
    val averageWorkoutsPerWeek: Double,
    val totalVolumeKg: Double,
    val totalCompletedSets: Int,
    val totalDurationSeconds: Long,
    val weeklyBreakdown: List<VolumeDataPoint>,
    val activeDaysCount: Int
)

data class ExerciseHistoryRecord(
    val sessionId: Long,
    val workoutName: String,
    val routineName: String,
    val completedAt: Long,
    val exerciseName: String,
    val completedSets: List<CompletedSetRecord>
)

data class CompletedSetRecord(
    val setNumber: Int,
    val setType: String,
    val weight: Double?,
    val reps: Double?,
    val durationSeconds: Int?,
    val rpe: Double?,
    val rir: Int?,
    val isCompleted: Boolean
)

