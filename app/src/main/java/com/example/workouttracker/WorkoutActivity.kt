package com.example.workouttracker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WorkoutActivity : AppCompatActivity(), WorkoutAdapter.WorkoutClickListener {

    private lateinit var recyclerViewWorkouts: RecyclerView
    private lateinit var adapter: WorkoutAdapter
    private lateinit var date: String
    private lateinit var tvDate: TextView
    private lateinit var deleteBtn: Button
    private lateinit var backBtn: Button
    private var isMultiSelectMode = false

    companion object {
        const val DELETE_WORKOUT_REQUEST_CODE = 1
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        date = intent.getStringExtra("date") ?: ""
        val workouts = loadWorkoutsForDate(date)

        recyclerViewWorkouts = findViewById(R.id.recyclerViewWorkouts)
        tvDate = findViewById(R.id.tvDate)
        deleteBtn = findViewById(R.id.deleteBtn)
        backBtn = findViewById(R.id.backBtn)

        setupRecyclerView(workouts)
        setupDateDisplay()
        setupButtons()

        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                resetSelectedWorkout()
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerView(workouts: List<Triple<String, List<Workout>, String>>) {
        adapter = WorkoutAdapter(workouts.toMutableList(), this)
        recyclerViewWorkouts.apply {
            this.adapter = this@WorkoutActivity.adapter
            layoutManager = LinearLayoutManager(this@WorkoutActivity)
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    resetSelectedWorkout()
                }
                false
            }
        }
    }


    private fun loadWorkoutsForDate(date: String): List<Triple<String, List<Workout>, String>> {
        val filesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: return emptyList()
        val workouts = mutableListOf<Triple<String, List<Workout>, String>>()
        val dateFormat = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        filesDir.listFiles()?.forEach { file ->
            if (file.nameWithoutExtension.startsWith(date) && file.extension == "csv") {
                val lines = file.readLines()
                val workoutMap = mutableMapOf<String, MutableList<WorkoutSet>>()

                var totalDuration = ""
                lines.forEachIndexed { index, line ->
                    if (index == 0) return@forEachIndexed
                    val parts = line.split(",")
                    if (parts[0] == "Total Duration") {
                        totalDuration = parts[1]
                    } else if (parts.size == 4) {
                        val exercise = parts[0]
                        val set = WorkoutSet(
                            setNumber = parts[1].toIntOrNull() ?: 0,
                            weight = parts[2].toDoubleOrNull() ?: 0.0,
                            repetitions = parts[3].toIntOrNull() ?: 0
                        )
                        workoutMap.getOrPut(exercise) { mutableListOf() }.add(set)
                    }
                }

                val workoutList = workoutMap.map { Workout(it.key, it.value) }

                val startTime = dateFormat.parse(file.nameWithoutExtension)
                val startTimeString = timeFormat.format(startTime)

                val durationParts = totalDuration.split(":")
                val durationInMillis = (durationParts[0].toLong() * 60 * 60 +
                        durationParts[1].toLong() * 60 +
                        durationParts[2].toLong()) * 1000

                val endTime = Date(startTime.time + durationInMillis)
                val endTimeString = timeFormat.format(endTime)

                val workoutTitle = "$startTimeString - $endTimeString"
                workouts.add(Triple(workoutTitle, workoutList, file.absolutePath))
            }
        }
        workouts.sortBy { it.third }
        return workouts
    }

    private fun setupDateDisplay() {
        val inputFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val parsedDate = inputFormat.parse(date)
            tvDate.text = outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            tvDate.text = date
        }
    }

    private fun setupButtons() {
        deleteBtn.visibility = View.GONE
        backBtn.setOnClickListener {
            if (adapter.getSelectedWorkoutsCount() == 1) {
                exitMultiSelectMode()
            }else if(adapter.getSelectedWorkoutsCount() > 1) {
                mergeWorkouts()
            }else {
                finish()
            }
        }
        deleteBtn.setOnClickListener { deleteSelectedWorkouts() }
    }

    override fun onWorkoutClicked(position: Int, filePath: String) {
        if (isMultiSelectMode) {
            adapter.toggleWorkoutSelection(position)
            updateButtonsVisibility()
            if (adapter.getSelectedWorkoutsCount() == 0) {
                exitMultiSelectMode()
            }
        } else {
            val intent = Intent(this, WorkoutDetailedActivity::class.java)
            intent.putExtra("WORKOUT_FILE_NAME", filePath)
            startActivityForResult(intent, DELETE_WORKOUT_REQUEST_CODE)
        }
    }

    override fun onWorkoutLongClicked(position: Int) {
        if (!isMultiSelectMode) {
            enterMultiSelectMode()
            adapter.toggleWorkoutSelection(position)
            updateButtonsVisibility()
        }
    }

    private fun enterMultiSelectMode() {
        isMultiSelectMode = true
        showAllButtons()
        adapter.enterMultiSelectMode()
    }

    private fun exitMultiSelectMode() {
        isMultiSelectMode = false
        resetSelectedWorkouts()
        adapter.exitMultiSelectMode()
        updateButtonsVisibility()
    }

    private fun showAllButtons() {
        deleteBtn.visibility = View.VISIBLE
        backBtn.text = "Cancel"
    }

    private fun updateButtonsVisibility() {
        val selectedCount = adapter.getSelectedWorkoutsCount()
        if (selectedCount > 0) {
            deleteBtn.visibility = View.VISIBLE
            deleteBtn.text = "Delete ${selectedCount} Workout${if (selectedCount > 1) "s" else ""}"
            if (selectedCount > 1) {
                backBtn.text = "Merge ${selectedCount} Workouts"
            } else {
                backBtn.text = "Cancel"
            }
        } else {
            deleteBtn.visibility = View.GONE
            backBtn.text = if (isMultiSelectMode) "Cancel" else "Back"
        }
    }

    private fun deleteSelectedWorkouts() {
        val selectedWorkouts = adapter.getSelectedWorkouts()
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to delete ${selectedWorkouts.size} workout${if (selectedWorkouts.size > 1) "s" else ""}?")
            .setPositiveButton("Yes") { _, _ ->
                selectedWorkouts.forEach { (_, _, filePath) ->
                    File(filePath).delete()
                }
                adapter.removeSelectedWorkouts()
                setResult(Activity.RESULT_OK)
                exitMultiSelectMode()
                if (adapter.itemCount == 0){
                    finish()
                }

            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun resetSelectedWorkouts() {
        adapter.clearSelections()
        updateButtonsVisibility()
    }

    private fun mergeWorkouts() {
        val selectedWorkouts = adapter.getSelectedWorkouts()
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to merge ${selectedWorkouts.size} workouts?")
            .setPositiveButton("Yes") { _, _ ->
                val sortedWorkouts = selectedWorkouts.sortedBy { workout ->
                    val fileName = File(workout.third).nameWithoutExtension
                    val timePart = fileName.split("_")[1]
                    SimpleDateFormat("HHmmss", Locale.getDefault()).parse(timePart)?.time ?: 0L
                }

                val earliestWorkoutFile = File(sortedWorkouts.first().third)
                val newFileName = earliestWorkoutFile.name
                val filesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val newFile = File(filesDir, newFileName)
                val mergedExercises = mutableListOf<Pair<String, WorkoutSet>>()
                var totalDurationMillis = 0L

                sortedWorkouts.forEach { (_, workouts, filePath) ->
                    var setNumber = 1
                    workouts.forEach { workout ->
                        workout.sets.forEach { set ->
                            mergedExercises.add(Pair(workout.exercise, set.copy(setNumber = setNumber++)))
                        }
                    }

                    val durationString = File(filePath).readLines().last().split(",")[1]
                    val durationParts = durationString.split(":")
                    val durationMillis = (durationParts[0].toLong() * 60 * 60 + durationParts[1].toLong() * 60 + durationParts[2].toLong()) * 1000
                    totalDurationMillis += durationMillis
                }

                val totalDuration = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalDurationMillis), TimeUnit.MILLISECONDS.toMinutes(totalDurationMillis) % 60, TimeUnit.MILLISECONDS.toSeconds(totalDurationMillis) % 60)

                newFile.bufferedWriter().use { writer ->
                    writer.write("Exercise,Set,Weight (KG),Repetitions\n")
                    mergedExercises.forEach { (exercise, set) ->
                        writer.write("$exercise,${set.setNumber},${set.weight},${set.repetitions}\n")
                    }
                    writer.write("Total Duration,$totalDuration\n")
                }

                sortedWorkouts.forEach { (_, _, filePath) ->
                    if (filePath != newFile.absolutePath) {
                        File(filePath).delete()
                    }
                }

                adapter.removeSelectedWorkouts()
                val newWorkouts = loadWorkoutsForDate(date)
                adapter.setWorkouts(newWorkouts.toMutableList())
                setResult(Activity.RESULT_OK)
                exitMultiSelectMode()
            }
            .setNegativeButton("No", null)
            .show()
    }


    private fun resetSelectedWorkout() {
        if (adapter.getSelectedWorkoutPosition() != -1) {
            adapter.setSelectedWorkoutPosition(-1)
            deleteBtn.visibility = View.GONE
            backBtn.text = "Back"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isMultiSelectMode) {
            exitMultiSelectMode()
        } else {
            super.onBackPressed()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DELETE_WORKOUT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val workouts = loadWorkoutsForDate(date)
            adapter.setWorkouts(workouts.toMutableList())
            setResult(Activity.RESULT_OK)
        }
    }
}










