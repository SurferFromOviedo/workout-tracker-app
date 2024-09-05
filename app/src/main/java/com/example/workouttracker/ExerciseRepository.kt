package com.example.workouttracker

import android.content.Context

object ExerciseRepository {
    private val exercises = mutableListOf<Exercise>()

    fun getExercises(): List<Exercise> = exercises.toList()

    fun addExercise(context: Context, exercise: Exercise) {
        if (exercises.none { it.name.equals(exercise.name, ignoreCase = true) }) {
            exercises.add(exercise)
            saveExercisesToSharedPreferences(context)
        }
    }

    fun removeExercise(context: Context, exercise: Exercise) {
        if (exercises.remove(exercise)) {
            saveExercisesToSharedPreferences(context)
        }
    }

    fun getBodyPartForExercise(exerciseName: String): String {
        return exercises.find { it.name == exerciseName }?.bodypart ?: "Unknown"
    }

    fun saveExercisesToSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("ExercisesPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val exerciseSet = exercises.map { "${it.name}|${it.bodypart}|${it.category}" }.toSet()
        editor.putStringSet("exercises", exerciseSet)
        editor.apply()
    }

    fun loadExercisesFromSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("ExercisesPrefs", Context.MODE_PRIVATE)
        val exerciseSet = sharedPreferences.getStringSet("exercises", null)

        exercises.clear()
        exerciseSet?.forEach { exerciseString ->
            val parts = exerciseString.split("|")
            if (parts.size == 3) {
                val exercise = Exercise(parts[0], parts[1], parts[2])
                exercises.add(exercise)
            }
        }
    }

    fun initializeDefaultExercises() {
        if (exercises.isEmpty()) {
            exercises.addAll(listOf(
                Exercise("Deadlift", "Whole Body", "Barbell"),
                Exercise("Hex Bar Deadlift", "Whole Body", "Barbell"),
                Exercise("Power Clean", "Whole Body", "Barbell"),
                Exercise("Romanian Deadlift", "Whole Body", "Barbell"),
                Exercise("Sumo Deadlift", "Whole Body", "Barbell"),
                Exercise("Clean and Jerk", "Whole Body", "Barbell"),
                Exercise("Snatch", "Whole Body", "Barbell"),
                Exercise("Clean", "Whole Body", "Barbell"),
                Exercise("Squat", "Legs", "Barbell"),
                Exercise("Front Squat", "Legs", "Barbell"),
                Exercise("Hip Thrust", "Legs", "Barbell"),
                Exercise("Box Squat", "Legs", "Barbell"),
                Exercise("Bulgarian Split Squat", "Legs", "Barbell"),
                Exercise("Smith Machine Squat", "Legs", "Barbell"),
                Exercise("Good Morning", "Legs", "Barbell"),
                Exercise("Zercher Squat", "Legs", "Barbell"),
                Exercise("Bent Over Row", "Back", "Barbell"),
                Exercise("Barbell Shrug", "Back", "Barbell"),
                Exercise("T Bar Row", "Back", "Barbell"),
                Exercise("Pendlay Row", "Back", "Barbell"),
                Exercise("Yates Row", "Back", "Barbell"),
                Exercise("Bench Pull", "Back", "Barbell"),
                Exercise("Barbell Pullover", "Back", "Barbell"),
                Exercise("Hex Bar Shrug", "Back", "Barbell"),
                Exercise("Bench Press", "Chest", "Barbell"),
                Exercise("Incline Bench Press", "Chest", "Barbell"),
                Exercise("Close Grip Bench Press", "Chest", "Barbell"),
                Exercise("Decline Bench Press", "Chest", "Barbell"),
                Exercise("Smith Machine Bench Press", "Chest", "Barbell"),
                Exercise("Floor Press", "Chest", "Barbell"),
                Exercise("Paused Bench Press", "Chest", "Barbell"),
                Exercise("Reverse Grip Bench Press", "Chest", "Barbell"),
                Exercise("Shoulder Press", "Shoulders", "Barbell"),
                Exercise("Military Press", "Shoulders", "Barbell"),
                Exercise("Seated Shoulder Press", "Shoulders", "Barbell"),
                Exercise("Push Press", "Shoulders", "Barbell"),
                Exercise("Upright Row", "Shoulders", "Barbell"),
                Exercise("Neck Curl", "Shoulders", "Barbell"),
                Exercise("Behind The Neck Press", "Shoulders", "Barbell"),
                Exercise("Barbell Front Raise", "Shoulders", "Barbell"),
                Exercise("Barbell Curl", "Biceps", "Barbell"),
                Exercise("EZ Bar Curl", "Biceps", "Barbell"),
                Exercise("Preacher Curl", "Biceps", "Barbell"),
                Exercise("Strict Curl", "Biceps", "Barbell"),
                Exercise("Spider Curl", "Biceps", "Barbell"),
                Exercise("Cheat Curl", "Biceps", "Barbell"),
                Exercise("Lying Tricep Extension", "Triceps", "Barbell"),
                Exercise("Tricep Extension", "Triceps", "Barbell"),
                Exercise("JM Press", "Triceps", "Barbell"),
                Exercise("Wrist Curl", "Forearms", "Barbell"),
                Exercise("Reverse Barbell Curl", "Forearms", "Barbell"),
                Exercise("Reverse Wrist Curl", "Forearms", "Barbell"),
                Exercise("Muscle Ups", "Whole Body", "Bodyweight"),
                Exercise("Burpees", "Whole Body", "Bodyweight"),
                Exercise("Ring Muscle Ups", "Whole Body", "Bodyweight"),
                Exercise("Clap Pull Up", "Whole Body", "Bodyweight"),
                Exercise("Squat Thrust", "Whole Body", "Bodyweight"),
                Exercise("Bodyweight Squat", "Legs", "Bodyweight"),
                Exercise("Single Leg Squat", "Legs", "Bodyweight"),
                Exercise("Pistol Squat", "Legs", "Bodyweight"),
                Exercise("Bodyweight Calf Raise", "Legs", "Bodyweight"),
                Exercise("Lunge", "Legs", "Bodyweight"),
                Exercise("Glute Bridge", "Legs", "Bodyweight"),
                Exercise("Reverse Lunge", "Legs", "Bodyweight"),
                Exercise("Squat Jump", "Legs", "Bodyweight"),
                Exercise("Pull Ups", "Back", "Bodyweight"),
                Exercise("Chin Ups", "Back", "Bodyweight"),
                Exercise("Neutral Grip Pull Ups", "Back", "Bodyweight"),
                Exercise("Back Extension", "Back", "Bodyweight"),
                Exercise("One Arm Pull Ups", "Back", "Bodyweight"),
                Exercise("Inverted Row", "Back", "Bodyweight"),
                Exercise("Reverse Hyperextension", "Back", "Bodyweight"),
                Exercise("Push Ups", "Chest", "Bodyweight"),
                Exercise("One Arm Push Ups", "Chest", "Bodyweight"),
                Exercise("Diamond Push Ups", "Chest", "Bodyweight"),
                Exercise("Decline Push Up", "Chest", "Bodyweight"),
                Exercise("Incline Push Up", "Chest", "Bodyweight"),
                Exercise("Close Grip Push Up", "Chest", "Bodyweight"),
                Exercise("Archer Push Ups", "Chest", "Bodyweight"),
                Exercise("Handstand Push Ups", "Shoulders", "Bodyweight"),
                Exercise("Pike Push Up", "Shoulders", "Bodyweight"),
                Exercise("Dips", "Triceps", "Bodyweight"),
                Exercise("Ring Dips", "Triceps", "Bodyweight"),
                Exercise("Bench Dips", "Triceps", "Bodyweight"),
                Exercise("Sit Ups", "Core", "Bodyweight"),
                Exercise("Crunches", "Core", "Bodyweight"),
                Exercise("Hanging Leg Raise", "Core", "Bodyweight"),
                Exercise("Russian Twist", "Core", "Bodyweight"),
                Exercise("Lying Leg Raise", "Core", "Bodyweight"),
                Exercise("Decline Sit Up", "Core", "Bodyweight"),
                Exercise("Hanging Knee Raise", "Core", "Bodyweight"),
                Exercise("Ab Wheel Rollout", "Core", "Bodyweight"),
                Exercise("Dumbbell Romanian Deadlift", "Whole Body", "Dumbbell"),
                Exercise("Dumbbell Deadlift", "Whole Body", "Dumbbell"),
                Exercise("Dumbbell Snatch", "Whole Body", "Dumbbell"),
                Exercise("Single Leg Dumbbell Deadlift", "Whole Body", "Dumbbell"),
                Exercise("Dumbbell Clean and Press", "Whole Body", "Dumbbell"),
                Exercise("Dumbbell Thruster", "Whole Body", "Dumbbell"),
                Exercise("Dumbbell High Pull", "Whole Body", "Dumbbell"),
                Exercise("Dumbbell Hang Clean", "Whole Body", "Dumbbell"),
                Exercise("Dumbbell Bulgarian Split Squat", "Legs", "Dumbbell"),
                Exercise("Goblet Squat", "Legs", "Dumbbell"),
                Exercise("Dumbbell Lunge", "Legs", "Dumbbell"),
                Exercise("Dumbbell Squat", "Legs", "Dumbbell"),
                Exercise("Dumbbell Calf Raise", "Legs", "Dumbbell"),
                Exercise("Dumbbell Front Squat", "Legs", "Dumbbell"),
                Exercise("Dumbbell Split Squat", "Legs", "Dumbbell"),
                Exercise("Dumbbell Walking Calf Raise", "Legs", "Dumbbell"),
                Exercise("Dumbbell Row", "Back", "Dumbbell"),
                Exercise("Dumbbell Shrug", "Back", "Dumbbell"),
                Exercise("Dumbbell Pullover", "Back", "Dumbbell"),
                Exercise("Dumbbell Reverse Fly", "Back", "Dumbbell"),
                Exercise("Chest Supported Dumbbell Row", "Back", "Dumbbell"),
                Exercise("Bent Over Dumbbell Row", "Back", "Dumbbell"),
                Exercise("Dumbbell Bench Pull", "Back", "Dumbbell"),
                Exercise("Renegade Row", "Back", "Dumbbell"),
                Exercise("Dumbbell Bench Press", "Chest", "Dumbbell"),
                Exercise("Incline Dumbbell Bench Press", "Chest", "Dumbbell"),
                Exercise("Dumbbell Fly", "Chest", "Dumbbell"),
                Exercise("Incline Dumbbell Fly", "Chest", "Dumbbell"),
                Exercise("Dumbbell Floor Press", "Chest", "Dumbbell"),
                Exercise("Decline Dumbbell Bench Press", "Chest", "Dumbbell"),
                Exercise("Close Grip Dumbbell Bench Press", "Chest", "Dumbbell"),
                Exercise("Decline Dumbbell Fly", "Chest", "Dumbbell"),
                Exercise("Dumbbell Shoulder Press", "Shoulders", "Dumbbell"),
                Exercise("Dumbbell Lateral Raise", "Shoulders", "Dumbbell"),
                Exercise("Seated Dumbbell Shoulder Press", "Shoulders", "Dumbbell"),
                Exercise("Dumbbell Front Raise", "Shoulders", "Dumbbell"),
                Exercise("Arnold Press", "Shoulders", "Dumbbell"),
                Exercise("Dumbbell Upright Row", "Shoulders", "Dumbbell"),
                Exercise("Dumbbell Z Press", "Shoulders", "Dumbbell"),
                Exercise("Dumbbell External Rotation", "Shoulders", "Dumbbell"),
                Exercise("Dumbbell Curl", "Biceps", "Dumbbell"),
                Exercise("Hammer Curl", "Biceps", "Dumbbell"),
                Exercise("Dumbbell Concentration Curl", "Biceps", "Dumbbell"),
                Exercise("Incline Dumbbell Curl", "Biceps", "Dumbbell"),
                Exercise("One Arm Dumbbell Preacher Curl", "Biceps", "Dumbbell"),
                Exercise("Incline Hammer Curl", "Biceps", "Dumbbell"),
                Exercise("Zottman Curl", "Biceps", "Dumbbell"),
                Exercise("Seated Dumbbell Curl", "Biceps", "Dumbbell"),
                Exercise("Dumbbell Tricep Extension", "Triceps", "Dumbbell"),
                Exercise("Lying Dumbbell Tricep Extension", "Triceps", "Dumbbell"),
                Exercise("Dumbbell Tricep Kickback", "Triceps", "Dumbbell"),
                Exercise("Seated Dumbbell Tricep Extension", "Triceps", "Dumbbell"),
                Exercise("Tate Press", "Triceps", "Dumbbell"),
                Exercise("Dumbbell Side Bend", "Core", "Dumbbell"),
                Exercise("Dumbbell Wrist Curl", "Forearms", "Dumbbell"),
                Exercise("Dumbbell Reverse Wrist Curl", "Forearms", "Dumbbell"),
                Exercise("Dumbbell Reverse Curl", "Forearms", "Dumbbell"),
                Exercise("Sled Leg Press", "Legs", "Machine"),
                Exercise("Leg Extension", "Legs", "Machine"),
                Exercise("Horizontal Leg Press", "Legs", "Machine"),
                Exercise("Hack Squat", "Legs", "Machine"),
                Exercise("Seated Leg Curl", "Legs", "Machine"),
                Exercise("Lying Leg Curl", "Legs", "Machine"),
                Exercise("Machine Calf Raise", "Legs", "Machine"),
                Exercise("Vertical Leg Press", "Legs", "Machine"),
                Exercise("Machine Row", "Back", "Machine"),
                Exercise("Machine Reverse Fly", "Back", "Machine"),
                Exercise("Machine Back Extension", "Back", "Machine"),
                Exercise("Machine Shrug", "Back", "Machine"),
                Exercise("Chest Press", "Chest", "Machine"),
                Exercise("Machine Chest Fly", "Chest", "Machine"),
                Exercise("Machine Shoulder Press", "Shoulders", "Machine"),
                Exercise("Machine Lateral Raise", "Shoulders", "Machine"),
                Exercise("Machine Bicep Curl", "Biceps", "Machine"),
                Exercise("Seated Dip Machine", "Triceps", "Machine"),
                Exercise("Machine Tricep Extension", "Triceps", "Machine"),
                Exercise("Machine Seated Crunch", "Core", "Machine"),
                Exercise("Cable Pull Through", "Legs", "Cable"),
                Exercise("Cable Kickback", "Legs", "Cable"),
                Exercise("Cable Leg Extension", "Legs", "Cable"),
                Exercise("Lat Pulldown", "Back", "Cable"),
                Exercise("Seated Cable Row", "Back", "Cable"),
                Exercise("Close Grip Lat Pulldown", "Back", "Cable"),
                Exercise("Reverse Grip Lat Pulldown", "Back", "Cable"),
                Exercise("Straight Arm Pulldown", "Back", "Cable"),
                Exercise("Cable Reverse Fly", "Back", "Cable"),
                Exercise("One Arm Lat Pulldown", "Back", "Cable"),
                Exercise("One Arm Seated Cable Row", "Back", "Cable"),
                Exercise("Cable Fly", "Chest", "Cable"),
                Exercise("Cable Lateral Raise", "Shoulders", "Cable"),
                Exercise("Face Pull", "Shoulders", "Cable"),
                Exercise("Cable External Rotation", "Shoulders", "Cable"),
                Exercise("Cable Bicep Curl", "Biceps", "Cable"),
                Exercise("One Arm Cable Bicep Curl", "Biceps", "Cable"),
                Exercise("Cable Hammer Curl", "Biceps", "Cable"),
                Exercise("One Arm Pulldown", "Biceps", "Cable"),
                Exercise("Overhead Cable Curl", "Biceps", "Cable"),
                Exercise("Incline Cable Curl", "Biceps", "Cable"),
                Exercise("Lying Cable Curl", "Biceps", "Cable"),
                Exercise("Tricep Pushdown", "Triceps", "Cable"),
                Exercise("Tricep Rope Pushdown", "Triceps", "Cable"),
                Exercise("Cable Overhead Tricep Extension", "Triceps", "Cable"),
                Exercise("Reverse Grip Tricep Pushdown", "Triceps", "Cable"),
                Exercise("Cable Crunch", "Core", "Cable"),
                Exercise("Cable Woodchopper", "Core", "Cable"),
                Exercise("High Pulley Crunch", "Core", "Cable"),
                Exercise("Standing Cable Crunch", "Core", "Cable"),
            ))
        }
    }
}

