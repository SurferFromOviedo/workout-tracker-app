package com.example.workouttracker

import java.util.UUID

data class Exercise(
    var name: String,
    val bodypart: String,
    val category: String,
    val sets: MutableList<Set> = mutableListOf(),
    val id: String = UUID.randomUUID().toString(),
)


data class Set(
    var weight: String = "",
    var reps: String = "",
    var unit: String = ""
)


