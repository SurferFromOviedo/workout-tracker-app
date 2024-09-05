package com.example.workouttracker


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.io.File
import java.io.FileWriter
import java.io.IOException

class StartActivity : AppCompatActivity(), ExerciseSelectionListener, SelectedExerciseAdapter.OnExerciseClickListener {
    private val sdfDate = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    private val sdfTime = SimpleDateFormat("HHmmss", Locale.getDefault())
    private lateinit var timeTextView: TextView
    private lateinit var handler: Handler
    private var startTime = 0L
    private var elapsedTime = 0L
    private var running = false

    private lateinit var recyclerView: RecyclerView
    private val selectedExercises = mutableListOf<Exercise>()
    private lateinit var adapter: SelectedExerciseAdapter

    private var selectedExerciseId: String? = null
    private lateinit var btnAddExe: Button
    private lateinit var btnEnd: Button

    private lateinit var exerciseUsageManager: ExerciseUsageManager

    private val PREFS_NAME = "WorkoutPrefs"
    private val KEY_EXERCISES = "exercises"
    private val KEY_START_TIME = "startTime"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        exerciseUsageManager = ExerciseUsageManager(this)

        btnAddExe = findViewById(R.id.btnAddExe)
        btnEnd = findViewById(R.id.btnEnd)

        btnAddExe.setOnClickListener {
            val dialogFragment = ExerciseDialogFragment()
            dialogFragment.show(supportFragmentManager, "ExerciseDialogFragment")
        }

        btnEnd.setOnClickListener {
            if (selectedExerciseId == null) {
                if(selectedExercises.isNotEmpty()){
                    showEndConfirmationDialog()
                }else {
                    finish()
                }
            } else {
                removeSetFromExercise(selectedExerciseId!!)
                resetSelectedExercise()
            }
        }

        timeTextView = findViewById(R.id.tw_date_time)
        handler = Handler(Looper.getMainLooper())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()

        findViewById<View>(R.id.main).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                resetSelectedExercise()
            }
            false
        }

        restoreWorkoutState()
        if (selectedExercises.isEmpty() && elapsedTime == 0L) {
            startNewWorkout()
        } else {
            startTimer()
        }
    }

    private fun startNewWorkout() {
        selectedExercises.clear()
        elapsedTime = 0L
        startTime = System.currentTimeMillis()
        startTimer()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (selectedExerciseId == null) {
            if (selectedExercises.isNotEmpty()) {
                super.onBackPressed()
            } else {
                clearWorkoutState()
                super.onBackPressed()
            }
        } else {
            resetSelectedExercise()
        }
    }

    override fun onPause() {
        super.onPause()
        if (running && selectedExercises.isNotEmpty()) {
            saveWorkoutState()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewSelectedExercises)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SelectedExerciseAdapter(this, selectedExercises, this)
        recyclerView.adapter = adapter

        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                hideKeyboard()
                resetSelectedExercise()
            }
            false
        }
    }

    override fun onExerciseSelected(exercise: Exercise) {
        val exerciseName = generateUniqueExerciseName(exercise.name)
        val newExercise = exercise.copy(
            id = UUID.randomUUID().toString(),
            name = exerciseName,
            sets = mutableListOf(Set())
        )

        selectedExercises.add(newExercise)
        adapter.notifyItemInserted(selectedExercises.size - 1)
    }

    private fun generateUniqueExerciseName(baseName: String): String {
        val existingNames = selectedExercises.map { it.name }
        if (!existingNames.contains(baseName)) return baseName

        var counter = 2
        var newName = "$baseName #$counter"
        while (existingNames.contains(newName)) {
            counter++
            newName = "$baseName #$counter"
        }
        return newName
    }

    override fun onExerciseClicked(exerciseId: String) {
        selectedExerciseId = exerciseId
        adapter.setSelectedExerciseId(exerciseId)
        updateButtonStates()
    }

    private fun updateButtonStates() {
        if (selectedExerciseId == null) {
            btnAddExe.text = "Add Exercise"
            btnAddExe.setOnClickListener {
                val dialogFragment = ExerciseDialogFragment()
                dialogFragment.show(supportFragmentManager, "ExerciseDialogFragment")
            }
            btnEnd.text = "End Workout"
            btnEnd.setOnClickListener {
                if(selectedExercises.isNotEmpty()){
                    showEndConfirmationDialog()
                }else {
                    clearWorkoutState()
                    finish()
                }
            }
        } else {
            btnAddExe.text = "Add Set"
            btnAddExe.setOnClickListener {
                addSetToExercise(selectedExerciseId!!)
            }
            btnEnd.text = "Remove Last Set"
            btnEnd.setOnClickListener {
                removeSetFromExercise(selectedExerciseId!!)
            }
        }
    }

    private fun resetSelectedExercise() {
        selectedExerciseId = null
        adapter.setSelectedExerciseId(null)
        updateButtonStates()
    }

    private fun addSetToExercise(exerciseId: String) {
        val exercise = selectedExercises.find { it.id == exerciseId } ?: return
        exercise.sets.add(Set())
        adapter.notifyItemChanged(selectedExercises.indexOf(exercise))
    }

    private fun removeSetFromExercise(exerciseId: String) {
        val exerciseIndex = selectedExercises.indexOfFirst { it.id == exerciseId }
        val exercise = selectedExercises.getOrNull(exerciseIndex) ?: return

        if (exercise.sets.size > 1) {
            exercise.sets.removeAt(exercise.sets.size - 1)
            adapter.notifyItemChanged(exerciseIndex)
        } else if (exercise.sets.size == 1) {
            exercise.sets.removeAt(0)
            selectedExercises.removeAt(exerciseIndex)
            reassignExerciseNames()
            adapter.notifyItemRemoved(exerciseIndex)
            if (selectedExercises.isEmpty()) {
                resetSelectedExercise()
            } else {
                if (selectedExerciseId == exerciseId) {
                    resetSelectedExercise()
                }
            }
        }
    }

    private fun reassignExerciseNames() {
        val baseNames = selectedExercises.map { it.name.split(" #")[0] }.distinct()

        for (baseName in baseNames) {
            val exercises = selectedExercises.filter { it.name.startsWith(baseName) }
            exercises.forEachIndexed { index, exercise ->
                exercise.name = if (index == 0) baseName else "$baseName #${index + 1}"
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun showEndConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to end the workout?")
            .setPositiveButton("Yes") { _, _ ->
                endWorkout()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun saveWorkoutToCSV() {
        val fileName = "${sdfDate.format(startTime)}_${sdfTime.format(startTime)}.csv"
        val filesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return
        val csvFile = File(filesDir, fileName)

        try {
            val writer = FileWriter(csvFile, true)

            val exerciseOrder = mutableListOf<Pair<String, Set>>()
            val setCounters = mutableMapOf<String, Int>()

            for (exercise in selectedExercises) {
                val baseName = exercise.name.split(" #")[0]
                for (set in exercise.sets) {
                    exerciseOrder.add(baseName to set)
                }
            }

            writer.append("Exercise,Set,Weight (KG),Repetitions\n")

            for ((baseName, set) in exerciseOrder) {
                val setCounter = setCounters.getOrDefault(baseName, 1)
                val weightInKG = if (set.unit == "KG") {
                    set.weight.toFloatOrNull() ?: 0f
                } else {
                    (set.weight.toFloatOrNull() ?: 0f) / 2.2046226218487757
                }
                val formattedWeight = String.format("%.3f", weightInKG)
                val formattedSetReps = set.reps.toIntOrNull() ?: 0
                writer.append("$baseName,$setCounter,$formattedWeight,${formattedSetReps}\n")
                setCounters[baseName] = setCounter + 1
            }

            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime/ (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60)) % 24

            val timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

            writer.append("Total Duration,$timeString\n")

            writer.flush()
            writer.close()

            showToast("Workout data saved successfully!")
            Log.d("WorkoutData", "File saved at: ${csvFile.absolutePath}")

            for (exercise in selectedExercises) {
                exerciseUsageManager.incrementUsage(exercise.name.split(" #")[0])
            }

        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Failed to save workout data!")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startTimer() {
        if (!running) {
            if (startTime == 0L) {
                startTime = System.currentTimeMillis()
            }
            elapsedTime = System.currentTimeMillis() - startTime
            handler.post(updateTimeTask)
            running = true
        }
    }

    private val updateTimeTask = object : Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startTime
            updateTimeText()
            handler.postDelayed(this, 1000)
        }
    }
    private fun updateTimeText() {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24

        val timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate: String = sdf.format(Date())

        timeTextView.text = "$currentDate\n   $timeString"
    }

    private fun saveWorkoutState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val exercisesJson = Gson().toJson(selectedExercises)
        editor.putString(KEY_EXERCISES, exercisesJson)

        editor.putLong(KEY_START_TIME, startTime)

        editor.apply()
    }

    private fun restoreWorkoutState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val exercisesJson = prefs.getString(KEY_EXERCISES, null)
        if (exercisesJson != null) {
            val type = object : TypeToken<List<Exercise>>() {}.type
            selectedExercises.clear()
            selectedExercises.addAll(Gson().fromJson(exercisesJson, type))
            adapter.notifyDataSetChanged()
        }

        startTime = prefs.getLong(KEY_START_TIME, 0L)
        if (startTime != 0L) {
            elapsedTime = System.currentTimeMillis() - startTime
        }
    }

    private fun clearWorkoutState() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        selectedExercises.clear()
        startTime = 0L
        elapsedTime = 0L
        running = false
        handler.removeCallbacks(updateTimeTask)
    }


    private fun endWorkout() {
        if (selectedExercises.isNotEmpty()) {
            saveWorkoutToCSV()
        }
        clearWorkoutState()
        finish()
    }
}
















