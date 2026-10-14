package com.sami.setra.domain

import com.sami.setra.data.local.database.entity.CompletedSetEntity
import com.sami.setra.data.local.database.relation.WorkoutSessionWithDetails
import com.sami.setra.data.model.ExerciseProgressionPoint
import com.sami.setra.data.model.PersonalRecord
import com.sami.setra.data.model.RecordType
import com.sami.setra.data.model.TimeFrame
import com.sami.setra.data.model.VolumeDataPoint
import com.sami.setra.data.model.WorkoutFrequencyStats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ProgressCalculator {

    fun calculatePersonalRecords(
        sessions: List<WorkoutSessionWithDetails>,
        exerciseIdFilter: String? = null,
        weightUnit: String = "kg"
    ): List<PersonalRecord> {
        val completedSessions = sessions.filter { it.session.status == "COMPLETED" }
        val recordsMap = mutableMapOf<String, MutableMap<RecordType, PersonalRecord>>()

        for (sessionWithDetails in completedSessions) {
            val session = sessionWithDetails.session
            val sessionTimestamp = session.completedAt ?: session.startedAt

            for (sessionEx in sessionWithDetails.sessionExercises) {
                val exerciseId = sessionEx.sessionExercise.exerciseId ?: continue
                if (exerciseIdFilter != null && exerciseId != exerciseIdFilter) continue

                val exerciseName = sessionEx.sessionExercise.exerciseNameSnapshot.ifBlank {
                    sessionEx.exercise?.name ?: "Exercise"
                }

                val exerciseRecords = recordsMap.getOrPut(exerciseId) { mutableMapOf() }

                for (set in sessionEx.completedSets) {
                    if (!set.isCompleted) continue

                    val weight = set.weight
                    val reps = set.reps
                    val duration = set.durationSeconds
                    val isWarmup = set.isWarmup || set.setType == "WARMUP"

                    // 1. Heaviest Weight (working set only)
                    if (!isWarmup && weight != null && weight > 0) {
                        val currentHeaviest = exerciseRecords[RecordType.HEAVIEST_WEIGHT]
                        if (currentHeaviest == null || weight > currentHeaviest.value) {
                            exerciseRecords[RecordType.HEAVIEST_WEIGHT] = PersonalRecord(
                                exerciseId = exerciseId,
                                exerciseName = exerciseName,
                                recordType = RecordType.HEAVIEST_WEIGHT,
                                value = weight,
                                unit = weightUnit,
                                associatedWeight = weight,
                                associatedReps = reps,
                                achievedAt = sessionTimestamp,
                                sessionId = session.id,
                                workoutName = session.workoutName
                            )
                        }
                    }

                    // 2. Max Reps at any non-zero weight or bodyweight
                    if (reps != null && reps > 0) {
                        val currentMaxReps = exerciseRecords[RecordType.MAX_REPS]
                        val isNewRecord = if (currentMaxReps == null) true else {
                            if (reps > currentMaxReps.value) true
                            else if (reps == currentMaxReps.value && (weight ?: 0.0) > (currentMaxReps.associatedWeight ?: 0.0)) true
                            else false
                        }

                        if (isNewRecord) {
                            exerciseRecords[RecordType.MAX_REPS] = PersonalRecord(
                                exerciseId = exerciseId,
                                exerciseName = exerciseName,
                                recordType = RecordType.MAX_REPS,
                                value = reps,
                                unit = "reps",
                                associatedWeight = weight,
                                associatedReps = reps,
                                achievedAt = sessionTimestamp,
                                sessionId = session.id,
                                workoutName = session.workoutName
                            )
                        }
                    }

                    // 3. Max Set Volume (weight * reps)
                    if (!isWarmup && weight != null && weight > 0 && reps != null && reps > 0) {
                        val setVolume = weight * reps
                        val currentMaxVol = exerciseRecords[RecordType.MAX_SET_VOLUME]
                        if (currentMaxVol == null || setVolume > currentMaxVol.value) {
                            exerciseRecords[RecordType.MAX_SET_VOLUME] = PersonalRecord(
                                exerciseId = exerciseId,
                                exerciseName = exerciseName,
                                recordType = RecordType.MAX_SET_VOLUME,
                                value = setVolume,
                                unit = weightUnit,
                                associatedWeight = weight,
                                associatedReps = reps,
                                achievedAt = sessionTimestamp,
                                sessionId = session.id,
                                workoutName = session.workoutName
                            )
                        }

                        // 4. Estimated 1RM (Brzycki formula for 1..12 reps)
                        if (reps in 1.0..12.0) {
                            val e1rm = if (reps == 1.0) weight else weight * (36.0 / (37.0 - reps))
                            val current1RM = exerciseRecords[RecordType.ESTIMATED_1RM]
                            if (current1RM == null || e1rm > current1RM.value) {
                                exerciseRecords[RecordType.ESTIMATED_1RM] = PersonalRecord(
                                    exerciseId = exerciseId,
                                    exerciseName = exerciseName,
                                    recordType = RecordType.ESTIMATED_1RM,
                                    value = e1rm,
                                    unit = weightUnit,
                                    associatedWeight = weight,
                                    associatedReps = reps,
                                    achievedAt = sessionTimestamp,
                                    sessionId = session.id,
                                    workoutName = session.workoutName
                                )
                            }
                        }
                    }

                    // 5. Max Duration (duration-based exercises)
                    if (duration != null && duration > 0) {
                        val currentMaxDuration = exerciseRecords[RecordType.MAX_DURATION]
                        if (currentMaxDuration == null || duration > currentMaxDuration.value.toInt()) {
                            exerciseRecords[RecordType.MAX_DURATION] = PersonalRecord(
                                exerciseId = exerciseId,
                                exerciseName = exerciseName,
                                recordType = RecordType.MAX_DURATION,
                                value = duration.toDouble(),
                                unit = "s",
                                associatedWeight = weight,
                                associatedReps = reps,
                                achievedAt = sessionTimestamp,
                                sessionId = session.id,
                                workoutName = session.workoutName
                            )
                        }
                    }
                }
            }
        }

        return recordsMap.values.flatMap { it.values }.sortedByDescending { it.achievedAt }
    }

    fun calculateWorkoutFrequency(
        sessions: List<WorkoutSessionWithDetails>,
        nowMillis: Long = System.currentTimeMillis()
    ): WorkoutFrequencyStats {
        val completedSessions = sessions.filter { it.session.status == "COMPLETED" }
        if (completedSessions.isEmpty()) {
            return WorkoutFrequencyStats(
                totalCompletedWorkouts = 0,
                workoutsThisWeek = 0,
                workoutsThisMonth = 0,
                averageWorkoutsPerWeek = 0.0,
                totalVolumeKg = 0.0,
                totalCompletedSets = 0,
                totalDurationSeconds = 0L,
                weeklyBreakdown = emptyList(),
                activeDaysCount = 0
            )
        }

        val oneWeekAgo = nowMillis - (7L * 24 * 3600 * 1000)
        val oneMonthAgo = nowMillis - (30L * 24 * 3600 * 1000)

        val workoutsThisWeek = completedSessions.count { (it.session.completedAt ?: it.session.startedAt) >= oneWeekAgo }
        val workoutsThisMonth = completedSessions.count { (it.session.completedAt ?: it.session.startedAt) >= oneMonthAgo }

        val earliestTimestamp = completedSessions.minOf { it.session.startedAt }
        val timespanWeeks = maxOf(1.0, (nowMillis - earliestTimestamp).toDouble() / (7.0 * 24 * 3600 * 1000))
        val avgWorkoutsPerWeek = completedSessions.size / timespanWeeks

        var totalVol = 0.0
        var totalSets = 0
        var totalDurationSec = 0L

        val activeDatesSet = mutableSetOf<String>()
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (s in completedSessions) {
            val sessionTime = s.session.completedAt ?: s.session.startedAt
            activeDatesSet.add(dateSdf.format(Date(sessionTime)))

            val dur = maxOf(0L, (s.session.completedAt ?: sessionTime) - s.session.startedAt) / 1000
            totalDurationSec += dur

            for (ex in s.sessionExercises) {
                for (set in ex.completedSets) {
                    if (set.isCompleted) {
                        totalSets++
                        if (set.weight != null && set.reps != null && set.weight > 0 && set.reps > 0) {
                            totalVol += set.weight * set.reps
                        }
                    }
                }
            }
        }

        // Build 7-day or 7-week breakdown for bar charts
        val weeklyBreakdown = mutableListOf<VolumeDataPoint>()
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val daySdf = SimpleDateFormat("EEE", Locale.getDefault())
        // Generate past 7 days breakdown
        for (i in 6 downTo 0) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = dayCal.timeInMillis
            val dayEnd = dayStart + (24 * 3600 * 1000)

            val daySessions = completedSessions.filter {
                val t = it.session.completedAt ?: it.session.startedAt
                t in dayStart..<dayEnd
            }

            var dayVol = 0.0
            var daySetsCount = 0
            for (ds in daySessions) {
                for (ex in ds.sessionExercises) {
                    for (st in ex.completedSets) {
                        if (st.isCompleted) {
                            daySetsCount++
                            if (st.weight != null && st.reps != null && st.weight > 0 && st.reps > 0) {
                                dayVol += st.weight * st.reps
                            }
                        }
                    }
                }
            }

            weeklyBreakdown.add(
                VolumeDataPoint(
                    timestamp = dayStart,
                    label = daySdf.format(dayCal.time),
                    volumeKg = dayVol,
                    setsCount = daySetsCount,
                    workoutsCount = daySessions.size
                )
            )
        }

        return WorkoutFrequencyStats(
            totalCompletedWorkouts = completedSessions.size,
            workoutsThisWeek = workoutsThisWeek,
            workoutsThisMonth = workoutsThisMonth,
            averageWorkoutsPerWeek = avgWorkoutsPerWeek,
            totalVolumeKg = totalVol,
            totalCompletedSets = totalSets,
            totalDurationSeconds = totalDurationSec,
            weeklyBreakdown = weeklyBreakdown,
            activeDaysCount = activeDatesSet.size
        )
    }

    fun calculateExerciseProgression(
        sessions: List<WorkoutSessionWithDetails>,
        exerciseId: String
    ): List<ExerciseProgressionPoint> {
        val completedSessions = sessions
            .filter { it.session.status == "COMPLETED" }
            .sortedBy { it.session.completedAt ?: it.session.startedAt }

        val progressionPoints = mutableListOf<ExerciseProgressionPoint>()

        for (sessionWithDetails in completedSessions) {
            val sessionTime = sessionWithDetails.session.completedAt ?: sessionWithDetails.session.startedAt
            val sessionEx = sessionWithDetails.sessionExercises.find { it.sessionExercise.exerciseId == exerciseId } ?: continue

            val completedSets = sessionEx.completedSets.filter { it.isCompleted }
            if (completedSets.isEmpty()) continue

            var maxWeight = 0.0
            var maxWeightReps = 0.0
            var sessionExVolume = 0.0
            var bestE1RM = 0.0

            for (set in completedSets) {
                val w = set.weight ?: 0.0
                val r = set.reps ?: 0.0
                if (w > 0 && r > 0) {
                    sessionExVolume += w * r
                    if (w > maxWeight) {
                        maxWeight = w
                        maxWeightReps = r
                    } else if (w == maxWeight && r > maxWeightReps) {
                        maxWeightReps = r
                    }

                    if (r in 1.0..12.0) {
                        val e1rm = if (r == 1.0) w else w * (36.0 / (37.0 - r))
                        if (e1rm > bestE1RM) {
                            bestE1RM = e1rm
                        }
                    }
                }
            }

            progressionPoints.add(
                ExerciseProgressionPoint(
                    timestamp = sessionTime,
                    workoutName = sessionWithDetails.session.workoutName,
                    sessionId = sessionWithDetails.session.id,
                    bestWeight = maxWeight,
                    bestReps = maxWeightReps,
                    totalVolume = sessionExVolume,
                    estimated1RM = if (bestE1RM > 0) bestE1RM else null,
                    completedSetsCount = completedSets.size
                )
            )
        }

        return progressionPoints
    }

    fun calculateVolumeByTimeFrame(
        sessions: List<WorkoutSessionWithDetails>,
        timeFrame: TimeFrame,
        nowMillis: Long = System.currentTimeMillis()
    ): List<VolumeDataPoint> {
        val completedSessions = sessions.filter { it.session.status == "COMPLETED" }
        val cutoffTime = if (timeFrame == TimeFrame.ALL) 0L else nowMillis - (timeFrame.days.toLong() * 24 * 3600 * 1000)

        val filteredSessions = completedSessions.filter {
            (it.session.completedAt ?: it.session.startedAt) >= cutoffTime
        }.sortedBy { it.session.completedAt ?: it.session.startedAt }

        if (filteredSessions.isEmpty()) return emptyList()

        val points = mutableListOf<VolumeDataPoint>()
        val dateSdf = SimpleDateFormat("MMM d", Locale.getDefault())

        // Group by calendar day or session
        for (sessionWithDetails in filteredSessions) {
            val sessionTime = sessionWithDetails.session.completedAt ?: sessionWithDetails.session.startedAt
            var sessionVol = 0.0
            var setsCount = 0

            for (ex in sessionWithDetails.sessionExercises) {
                for (st in ex.completedSets) {
                    if (st.isCompleted) {
                        setsCount++
                        if (st.weight != null && st.reps != null && st.weight > 0 && st.reps > 0) {
                            sessionVol += st.weight * st.reps
                        }
                    }
                }
            }

            points.add(
                VolumeDataPoint(
                    timestamp = sessionTime,
                    label = dateSdf.format(Date(sessionTime)),
                    volumeKg = sessionVol,
                    setsCount = setsCount,
                    workoutsCount = 1
                )
            )
        }

        return points
    }
}
