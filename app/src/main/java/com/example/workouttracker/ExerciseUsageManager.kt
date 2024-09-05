package com.example.workouttracker

import android.content.Context
import java.io.File
import java.io.FileWriter

class ExerciseUsageManager(private val context: Context) {
    private val fileName = "exercise_usage.csv"
    private val file: File
        get() = File(context.filesDir, fileName)

    fun getExerciseUsages(): List<ExerciseUsage> {
        if (!file.exists()) return emptyList()

        return file.readLines().drop(1).map { line ->
            val (name, count) = line.split(",")
            ExerciseUsage(name, count.toInt())
        }
    }

    private fun saveExerciseUsages(usages: List<ExerciseUsage>) {
        FileWriter(file).use { writer ->
            writer.write("Exercise,Count\n")
            usages.forEach { usage ->
                writer.write("${usage.name},${usage.count}\n")
            }
        }
    }

    fun incrementUsage(exerciseName: String) {
        val usages = getExerciseUsages().toMutableList()
        val usage = usages.find { it.name == exerciseName }
        if (usage != null) {
            usage.count++
        } else {
            usages.add(ExerciseUsage(exerciseName, 1))
        }
        saveExerciseUsages(usages)
    }

}