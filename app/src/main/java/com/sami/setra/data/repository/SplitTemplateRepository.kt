package com.sami.setra.data.repository

import com.sami.setra.data.model.SplitExerciseTemplate
import com.sami.setra.data.model.SplitTemplate
import com.sami.setra.data.model.SplitTemplateDay

private fun ex(id: String, name: String, sets: String, reps: String) =
    SplitExerciseTemplate(id, name, sets, reps)

class SplitTemplateRepository {

    val builtInSplits: List<SplitTemplate> = listOf(
        SplitTemplate(
            id = "upper_lower",
            name = "Upper / Lower",
            description = "A four-day example that trains upper and lower body twice weekly. This is a reference template; exercise selection and schedules can be adapted.",
            targetFrequency = "4 Days / Week",
            category = "Hypertrophy & Strength",
            imageKey = "upper_lower",
            days = listOf(
                SplitTemplateDay(1, "Monday", false, "Upper A", "Upper-body session", listOf(
                    ex("Barbell_Bench_Press_-_Medium_Grip", "Barbell bench press", "3", "5-8"),
                    ex("Bent_Over_Barbell_Row", "Chest-supported row (barbell row example)", "3", "6-10"),
                    ex("Wide-Grip_Lat_Pulldown", "Lat pulldown (or pull-up)", "3", "8-12"),
                    ex("Seated_Dumbbell_Press", "Seated dumbbell shoulder press", "2-3", "8-12"),
                    ex("Cable_Seated_Lateral_Raise", "Cable lateral raise", "2-3", "12-20"),
                    ex("Triceps_Pushdown", "Triceps pushdown", "2-3", "10-15"),
                    ex("Dumbbell_Bicep_Curl", "Dumbbell curl", "2-3", "10-15")
                )),
                SplitTemplateDay(2, "Tuesday", false, "Lower A", "Lower-body session", listOf(
                    ex("Barbell_Squat", "Back squat", "3", "5-8"),
                    ex("Romanian_Deadlift", "Romanian deadlift", "3", "6-10"),
                    ex("Leg_Press", "Leg press", "2-3", "10-15"),
                    ex("Lying_Leg_Curls", "Lying leg curl", "2-3", "10-15"),
                    ex("Standing_Calf_Raises", "Standing calf raise", "3", "10-15"),
                    ex("Cable_Crunch", "Cable crunch", "2-3", "10-15")
                )),
                SplitTemplateDay(3, "Wednesday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(4, "Thursday", false, "Upper B", "Upper-body session", listOf(
                    ex("Incline_Dumbbell_Press", "Incline dumbbell press", "3", "8-12"),
                    ex("Wide-Grip_Lat_Pulldown", "Pull-up (or lat pulldown)", "3", "8-12"),
                    ex("Seated_Cable_Rows", "Seated cable row", "3", "8-12"),
                    ex("Leverage_Chest_Press", "Machine chest press", "2-3", "8-12"),
                    ex("Reverse_Flyes", "Reverse pec deck / rear-delt fly", "2-3", "12-20"),
                    ex("Cable_Rope_Overhead_Triceps_Extension", "Overhead triceps extension", "2-3", "10-15"),
                    ex("EZ-Bar_Curl", "EZ-bar curl", "2-3", "10-15")
                )),
                SplitTemplateDay(5, "Friday", false, "Lower B", "Lower-body session", listOf(
                    ex("Barbell_Deadlift", "Deadlift variation", "2-3", "5-8"),
                    ex("Front_Squat_Clean_Grip", "Front squat", "3", "6-10"),
                    ex("Split_Squat_with_Dumbbells", "Bulgarian split squat", "2-3", "8-12 per leg"),
                    ex("Seated_Leg_Curl", "Leg curl", "2-3", "10-15"),
                    ex("Seated_Calf_Raise", "Seated calf raise", "3", "10-15"),
                    ex("Plank", "Plank", "2-3", "sets")
                )),
                SplitTemplateDay(6, "Saturday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(7, "Sunday", true, "Rest Day", "Recovery")
            )
        ),
        SplitTemplate(
            id = "push_pull_legs",
            name = "Push / Pull / Legs",
            description = "A six-day example repeating Push, Pull, Legs twice per week. It can also be run as a three-day sequence; six days is not required.",
            targetFrequency = "6 Days / Week",
            category = "Bodybuilding & Muscle Building",
            imageKey = "push_pull_legs",
            days = listOf(
                SplitTemplateDay(1, "Monday", false, "Push A", "Chest, shoulders and triceps", listOf(
                    ex("Barbell_Bench_Press_-_Medium_Grip", "Barbell bench press", "3", "5-8"),
                    ex("Incline_Dumbbell_Press", "Incline dumbbell press", "3", "8-12"),
                    ex("Dumbbell_Shoulder_Press", "Seated shoulder press", "2-3", "6-10"),
                    ex("Cable_Seated_Lateral_Raise", "Cable lateral raise", "3", "12-20"),
                    ex("Triceps_Pushdown", "Cable triceps pushdown", "3", "10-15")
                )),
                SplitTemplateDay(2, "Tuesday", false, "Pull A", "Back, rear delts and biceps", listOf(
                    ex("Weighted_Pull_Ups", "Pull-up (or lat pulldown)", "3", "6-12"),
                    ex("Bent_Over_Barbell_Row", "Barbell row", "3", "6-10"),
                    ex("Seated_Cable_Rows", "Seated cable row", "2-3", "8-12"),
                    ex("Reverse_Flyes", "Reverse pec deck", "2-3", "12-20"),
                    ex("EZ-Bar_Curl", "Dumbbell or EZ-bar curl", "3", "8-15")
                )),
                SplitTemplateDay(3, "Wednesday", false, "Legs A", "Quads, hamstrings and calves", listOf(
                    ex("Barbell_Squat", "Back squat", "3", "5-8"),
                    ex("Romanian_Deadlift", "Romanian deadlift", "3", "6-10"),
                    ex("Leg_Press", "Leg press", "2-3", "10-15"),
                    ex("Seated_Leg_Curl", "Leg curl", "2-3", "10-15"),
                    ex("Standing_Calf_Raises", "Standing calf raise", "3", "10-15")
                )),
                SplitTemplateDay(4, "Thursday", false, "Push B", "Chest, shoulders and triceps", listOf(
                    ex("Incline_Dumbbell_Press", "Incline barbell or dumbbell press", "3", "6-10"),
                    ex("Leverage_Chest_Press", "Machine chest press", "3", "8-12"),
                    ex("Side_Lateral_Raise", "Dumbbell lateral raise", "3", "12-20"),
                    ex("Seated_Cable_Shoulder_Press", "Cable or machine shoulder press", "2-3", "8-12"),
                    ex("Cable_Rope_Overhead_Triceps_Extension", "Overhead triceps extension", "3", "10-15")
                )),
                SplitTemplateDay(5, "Friday", false, "Pull B", "Back, rear delts and biceps", listOf(
                    ex("Bent_Over_Barbell_Row", "Chest-supported row", "3", "8-12"),
                    ex("Wide-Grip_Lat_Pulldown", "Neutral-grip pulldown", "3", "8-12"),
                    ex("Seated_Cable_Rows", "One-arm cable row", "2-3", "10-12"),
                    ex("Cable_Rear_Delt_Fly", "Rear-delt fly", "3", "12-20"),
                    ex("Alternate_Hammer_Curl", "Hammer curl", "3", "10-15")
                )),
                SplitTemplateDay(6, "Saturday", false, "Legs B", "Quads, hamstrings, glutes and calves", listOf(
                    ex("Hack_Squat", "Front squat or hack squat", "3", "6-10"),
                    ex("Barbell_Hip_Thrust", "Hip thrust", "3", "8-12"),
                    ex("Split_Squat_with_Dumbbells", "Bulgarian split squat", "2-3", "8-12 per leg"),
                    ex("Seated_Leg_Curl", "Seated leg curl", "3", "10-15"),
                    ex("Seated_Calf_Raise", "Seated calf raise", "3", "10-15")
                )),
                SplitTemplateDay(7, "Sunday", true, "Rest Day", "Recovery")
            )
        ),
        SplitTemplate(
            id = "bro_split",
            name = "Body Part Split",
            description = "A five-day bodybuilding-style example emphasizing one major body part or region per session. Commonly called a Bro Split; this is a sample template, not a universal official routine.",
            targetFrequency = "5 Days / Week",
            category = "Commonly called Bro Split",
            imageKey = "bro_split",
            days = listOf(
                SplitTemplateDay(1, "Monday", false, "Chest", "Chest session", listOf(
                    ex("Barbell_Bench_Press_-_Medium_Grip", "Barbell bench press", "3", "5-8"),
                    ex("Incline_Dumbbell_Press", "Incline dumbbell press", "3", "8-12"),
                    ex("Leverage_Chest_Press", "Machine chest press", "2-3", "8-12"),
                    ex("Flat_Bench_Cable_Flyes", "Cable fly", "2-3", "12-15")
                )),
                SplitTemplateDay(2, "Tuesday", false, "Back", "Back session", listOf(
                    ex("Weighted_Pull_Ups", "Pull-up (or lat pulldown)", "3", "6-12"),
                    ex("Bent_Over_Barbell_Row", "Chest-supported row", "3", "6-10"),
                    ex("Seated_Cable_Rows", "Seated cable row", "2-3", "8-12"),
                    ex("Straight-Arm_Pulldown", "Straight-arm pulldown", "2-3", "12-15"),
                    ex("Hyperextensions_Back_Extensions", "Back extension (if appropriate)", "2-3", "10-15")
                )),
                SplitTemplateDay(3, "Wednesday", false, "Shoulders", "Shoulders session", listOf(
                    ex("Barbell_Shoulder_Press", "Seated overhead press", "3", "6-10"),
                    ex("Side_Lateral_Raise", "Dumbbell lateral raise", "3", "12-20"),
                    ex("Reverse_Flyes", "Reverse pec deck", "3", "12-20"),
                    ex("Cable_Seated_Lateral_Raise", "Cable lateral raise", "2", "12-20")
                )),
                SplitTemplateDay(4, "Thursday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(5, "Friday", false, "Legs", "Legs session", listOf(
                    ex("Barbell_Squat", "Back squat or hack squat", "3", "5-10"),
                    ex("Romanian_Deadlift", "Romanian deadlift", "3", "6-10"),
                    ex("Leg_Press", "Leg press", "2-3", "10-15"),
                    ex("Seated_Leg_Curl", "Leg curl", "3", "10-15"),
                    ex("Standing_Calf_Raises", "Standing or seated calf raise", "3", "10-15")
                )),
                SplitTemplateDay(6, "Saturday", false, "Arms", "Arms session", listOf(
                    ex("EZ-Bar_Curl", "EZ-bar curl", "3", "8-12"),
                    ex("Incline_Dumbbell_Curl", "Incline dumbbell curl", "2-3", "10-15"),
                    ex("Triceps_Pushdown", "Triceps pushdown", "3", "10-15"),
                    ex("Cable_Rope_Overhead_Triceps_Extension", "Overhead triceps extension", "2-3", "10-15"),
                    ex("Alternate_Hammer_Curl", "Hammer curl", "2", "10-15")
                )),
                SplitTemplateDay(7, "Sunday", true, "Rest Day", "Recovery")
            )
        ),
        SplitTemplate(
            id = "full_body",
            name = "Full Body",
            description = "A three-day alternating A/B full-body example. Week one runs A/B/A; alternate the order the following week (B/A/B).",
            targetFrequency = "3 Days / Week",
            category = "Strength & General Fitness",
            imageKey = "full_body",
            days = listOf(
                SplitTemplateDay(1, "Monday", false, "Full Body A", "Full-body session A", listOf(
                    ex("Barbell_Squat", "Back squat", "3", "5-8"),
                    ex("Barbell_Bench_Press_-_Medium_Grip", "Bench press", "3", "5-8"),
                    ex("Bent_Over_Barbell_Row", "Chest-supported row", "3", "8-12"),
                    ex("Romanian_Deadlift", "Romanian deadlift", "2-3", "6-10"),
                    ex("Side_Lateral_Raise", "Lateral raise", "2", "12-20"),
                    ex("Dumbbell_Bicep_Curl", "Dumbbell curl", "2", "10-15"),
                    ex("Triceps_Pushdown", "Triceps pushdown", "2", "10-15")
                )),
                SplitTemplateDay(2, "Tuesday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(3, "Wednesday", false, "Full Body B", "Full-body session B", listOf(
                    ex("Barbell_Deadlift", "Deadlift variation or hip thrust", "2-3", "5-8"),
                    ex("Incline_Dumbbell_Press", "Incline dumbbell press", "3", "8-12"),
                    ex("Wide-Grip_Lat_Pulldown", "Lat pulldown (or pull-up)", "3", "8-12"),
                    ex("Split_Squat_with_Dumbbells", "Split squat", "3", "8-12"),
                    ex("Seated_Leg_Curl", "Leg curl", "2-3", "10-15"),
                    ex("Cable_Rear_Delt_Fly", "Rear-delt fly", "2", "12-20"),
                    ex("Standing_Calf_Raises", "Calf raise or trunk exercise", "2-3", "10-15")
                )),
                SplitTemplateDay(4, "Thursday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(5, "Friday", false, "Full Body A", "Repeat session A; alternate to B/A/B the following week", listOf(
                    ex("Barbell_Squat", "Back squat", "3", "5-8"),
                    ex("Barbell_Bench_Press_-_Medium_Grip", "Bench press", "3", "5-8"),
                    ex("Bent_Over_Barbell_Row", "Chest-supported row", "3", "8-12"),
                    ex("Romanian_Deadlift", "Romanian deadlift", "2-3", "6-10"),
                    ex("Side_Lateral_Raise", "Lateral raise", "2", "12-20"),
                    ex("Dumbbell_Bicep_Curl", "Dumbbell curl", "2", "10-15"),
                    ex("Triceps_Pushdown", "Triceps pushdown", "2", "10-15")
                )),
                SplitTemplateDay(6, "Saturday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(7, "Sunday", true, "Rest Day", "Recovery")
            )
        ),
        SplitTemplate(
            id = "arnold_split",
            name = "Arnold Split",
            description = "A six-day example repeating chest/back, shoulders/arms, and legs. Exercise selections and exact historical routines varied; this is a practical template, not one definitive historical routine.",
            targetFrequency = "6 Days / Week",
            category = "Classic Physique & Volume",
            imageKey = "arnold_split",
            days = listOf(
                SplitTemplateDay(1, "Monday", false, "Chest and Back A", "Chest and back session", listOf(
                    ex("Barbell_Bench_Press_-_Medium_Grip", "Barbell bench press", "3", "6-10"),
                    ex("Wide-Grip_Lat_Pulldown", "Pull-up (or lat pulldown)", "3", "6-12"),
                    ex("Incline_Dumbbell_Press", "Incline dumbbell press", "3", "8-12"),
                    ex("Bent_Over_Barbell_Row", "Barbell row", "3", "8-12"),
                    ex("Flat_Bench_Cable_Flyes", "Cable fly", "2", "12-15"),
                    ex("Straight-Arm_Pulldown", "Straight-arm pulldown", "2", "12-15")
                )),
                SplitTemplateDay(2, "Tuesday", false, "Shoulders and Arms A", "Shoulders and arms session", listOf(
                    ex("Barbell_Shoulder_Press", "Seated overhead press", "3", "6-10"),
                    ex("Side_Lateral_Raise", "Lateral raise", "3", "12-20"),
                    ex("Cable_Rear_Delt_Fly", "Rear-delt fly", "2-3", "12-20"),
                    ex("EZ-Bar_Curl", "EZ-bar curl", "3", "8-12"),
                    ex("Incline_Dumbbell_Curl", "Incline dumbbell curl", "2", "10-15"),
                    ex("Close-Grip_Barbell_Bench_Press", "Close-grip bench press (or dip)", "3", "6-10"),
                    ex("Triceps_Pushdown", "Triceps pushdown", "2", "10-15")
                )),
                SplitTemplateDay(3, "Wednesday", false, "Legs A", "Legs session", listOf(
                    ex("Barbell_Squat", "Back squat", "3", "5-8"),
                    ex("Romanian_Deadlift", "Romanian deadlift", "3", "6-10"),
                    ex("Leg_Press", "Leg press", "2-3", "10-15"),
                    ex("Seated_Leg_Curl", "Leg curl", "2-3", "10-15"),
                    ex("Standing_Calf_Raises", "Calf raise", "3", "10-15")
                )),
                SplitTemplateDay(4, "Thursday", false, "Chest and Back B", "Chest and back session", listOf(
                    ex("Incline_Dumbbell_Press", "Incline barbell or dumbbell press", "3", "6-10"),
                    ex("Bent_Over_Barbell_Row", "Chest-supported row", "3", "8-12"),
                    ex("Leverage_Chest_Press", "Machine chest press", "3", "8-12"),
                    ex("Wide-Grip_Lat_Pulldown", "Neutral-grip pulldown", "3", "8-12"),
                    ex("Flat_Bench_Cable_Flyes", "Pec deck or cable fly", "2", "12-15"),
                    ex("Seated_Cable_Rows", "Seated cable row", "2", "10-12")
                )),
                SplitTemplateDay(5, "Friday", false, "Shoulders and Arms B", "Shoulders and arms session", listOf(
                    ex("Dumbbell_Shoulder_Press", "Machine or dumbbell shoulder press", "3", "8-12"),
                    ex("Cable_Seated_Lateral_Raise", "Cable lateral raise", "3", "12-20"),
                    ex("Reverse_Flyes", "Reverse pec deck", "3", "12-20"),
                    ex("Dumbbell_Bicep_Curl", "Dumbbell or preacher curl", "3", "10-15"),
                    ex("Alternate_Hammer_Curl", "Hammer curl", "2", "10-15"),
                    ex("Cable_Rope_Overhead_Triceps_Extension", "Overhead triceps extension", "3", "10-15"),
                    ex("Triceps_Pushdown", "Triceps pushdown", "2", "10-15")
                )),
                SplitTemplateDay(6, "Saturday", false, "Legs B", "Legs session", listOf(
                    ex("Hack_Squat", "Front squat or hack squat", "3", "6-10"),
                    ex("Barbell_Hip_Thrust", "Hip thrust", "3", "8-12"),
                    ex("Split_Squat_with_Dumbbells", "Bulgarian split squat", "2-3", "8-12 per leg"),
                    ex("Seated_Leg_Curl", "Seated leg curl", "3", "10-15"),
                    ex("Seated_Calf_Raise", "Seated calf raise", "3", "10-15")
                )),
                SplitTemplateDay(7, "Sunday", true, "Rest Day", "Recovery")
            )
        ),
        SplitTemplate(
            id = "custom",
            name = "Custom Split",
            description = "A flexible seven-day template for designing a personalized training week.",
            targetFrequency = "Custom Days / Week",
            category = "Personalized",
            imageKey = "custom",
            days = listOf(
                SplitTemplateDay(1, "Monday", false, "Workout Day 1", "Custom workout session"),
                SplitTemplateDay(2, "Tuesday", false, "Workout Day 2", "Custom workout session"),
                SplitTemplateDay(3, "Wednesday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(4, "Thursday", false, "Workout Day 3", "Custom workout session"),
                SplitTemplateDay(5, "Friday", false, "Workout Day 4", "Custom workout session"),
                SplitTemplateDay(6, "Saturday", true, "Rest Day", "Recovery"),
                SplitTemplateDay(7, "Sunday", true, "Rest Day", "Recovery")
            )
        )
    )

    fun getSplitById(splitId: String): SplitTemplate? = builtInSplits.find { it.id == splitId }
}
