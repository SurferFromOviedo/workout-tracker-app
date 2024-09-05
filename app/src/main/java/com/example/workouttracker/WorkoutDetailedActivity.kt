package com.example.workouttracker

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutDetailedActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private val selectedExercises = mutableListOf<Exercise>()
    private lateinit var adapter: ReadOnlySelectedExerciseAdapter
    private lateinit var btnExit: Button
    private lateinit var deleteBtn: Button

    private var workoutFileName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_detailed)

        workoutFileName = intent.getStringExtra("WORKOUT_FILE_NAME") ?: ""

        timeTextView = findViewById(R.id.tw_date_time)
        btnExit = findViewById(R.id.btnExit)
        deleteBtn = findViewById(R.id.btnDelete)

        setupRecyclerView()
        loadWorkoutFromCSV()

        btnExit.setOnClickListener {
            finish()
        }

        deleteBtn.setOnClickListener {
            deleteWorkout()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewSelectedExercises)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReadOnlySelectedExerciseAdapter(this, selectedExercises)
        recyclerView.adapter = adapter
    }

    private fun loadWorkoutFromCSV() {
        val filesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val fileName = workoutFileName.substringAfterLast("/")
        val fileNameWithoutCsv = fileName.substringBeforeLast(".")
        val fileTime =fileNameWithoutCsv.substringAfterLast("_")
        val fileDate = fileName.substringBefore("_")

        val inputFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDate = inputFormat.parse(fileDate)
        val date = outputFormat.format(parsedDate!!)

        val inputFormatTime = SimpleDateFormat("HHmmss", Locale.getDefault())
        val outputFormatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTimeParsed = inputFormatTime.parse(fileTime)
        val startTime = outputFormatTime.format(startTimeParsed!!)


        Log.d("WorkoutDetailedActivity", "File name: $fileDate")
        val file = File(filesDir, fileName)
        if (!file.exists()) {
            return
        }
        selectedExercises.clear()
        var totalDuration = ""
        val exerciseNames = mutableListOf<String>()
        val exerciseSets = mutableMapOf<String, MutableList<Set>>()

        file.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size == 4) {
                    val exerciseName = parts[0]
                    val weight = parts[2].toFloatOrNull()?.let {
                        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                    } ?: "0"
                    val reps = parts[3]

                    val uniqueExerciseName = generateUniqueExerciseName(exerciseName, exerciseNames)

                    if (!exerciseNames.contains(exerciseName)){
                        exerciseNames.add(exerciseName)
                        exerciseSets.getOrPut(exerciseName) { mutableListOf() }
                            .add(Set(weight = weight, reps = reps))
                    }else if (exerciseNames.contains(exerciseName) && removeNumberFromExerciseName(exerciseNames.last()) == exerciseName){
                        exerciseSets.getOrPut(exerciseNames.last()) { mutableListOf() }
                            .add(Set(weight = weight, reps = reps))
                    }else{
                        exerciseNames.add(uniqueExerciseName)
                        exerciseSets.getOrPut(uniqueExerciseName) { mutableListOf() }
                            .add(Set(weight = weight, reps = reps))
                    }

                } else if (parts.size == 2 && parts[0] == "Total Duration") {
                    totalDuration = parts[1]
                }
            }


        }

        val durationParts = totalDuration.split(":")
        val durationInMillis = (durationParts[0].toLong() * 60 * 60 +
                durationParts[1].toLong() * 60 +
                durationParts[2].toLong()) * 1000

        val startTimeMillis = startTimeParsed.time
        val endTimeMillis = startTimeMillis + durationInMillis
        val endTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(endTimeMillis))


        selectedExercises.addAll(exerciseNames.map { name ->
            Exercise(
                name = name,
                bodypart = "Unknown",
                category = "Unknown",
                sets = exerciseSets[name] ?: mutableListOf()
            )
        })

        adapter.notifyDataSetChanged()
        timeTextView.text = "$date $startTime - $endTime\n                 $totalDuration"
    }

    private fun generateUniqueExerciseName(baseName: String, exerciseNames: List<String>): String {
        var uniqueName = baseName
        var count = 2
        while (exerciseNames.contains(uniqueName)) {
            uniqueName = "$baseName #$count"
            count++
        }
        return uniqueName
    }

    private fun removeNumberFromExerciseName(exerciseName: String): String {
        return exerciseName.split("#")[0].trim()
    }

    private fun deleteWorkout() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete this workout?")
            .setPositiveButton("Yes") { _, _ ->
                File(workoutFileName).delete()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

}

