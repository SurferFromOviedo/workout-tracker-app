package com.example.workouttracker

data class Workout(
    val exercise: String,
    val sets: List<WorkoutSet>
)

data class WorkoutSet(
    val setNumber: Int,
    val weight: Double,
    val repetitions: Int
)

