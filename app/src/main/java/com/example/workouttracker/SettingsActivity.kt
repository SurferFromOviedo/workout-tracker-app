package com.example.workouttracker

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity(), ExerciseSelectionListener {

    private lateinit var addExerciseBtn: Button
    private lateinit var deleteExerciseBtn: Button
    private lateinit var kgBtn: Button
    private lateinit var lbBtn: Button
    private lateinit var backBtn: Button
    private var selectedExercise: Exercise? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("user_settings", MODE_PRIVATE)
        addExerciseBtn = findViewById(R.id.addExerciseBtn)
        deleteExerciseBtn = findViewById(R.id.deleteExerciseBtn)
        kgBtn = findViewById(R.id.kgBtn)
        lbBtn = findViewById(R.id.lbBtn)
        backBtn = findViewById(R.id.backBtn)

        loadSettings()

        addExerciseBtn.setOnClickListener {
            val addExerciseDialog = AddExerciseDialogFragment()
            addExerciseDialog.show(supportFragmentManager, "AddExerciseDialogFragment")
        }

        deleteExerciseBtn.setOnClickListener {
            val dialogFragment = ExerciseDeleteDialogFragment()
            dialogFragment.show(supportFragmentManager, "ExerciseDialogFragment")
        }

        kgBtn.setOnClickListener {
            kgBtn.isSelected = true
            lbBtn.isSelected = false
            val editor = sharedPreferences.edit()
            editor.putBoolean("useKg", kgBtn.isSelected)
            editor.apply()
            kgBtn.setBackgroundResource(R.drawable.exercise_background_pressed)
            lbBtn.setBackgroundResource(R.drawable.dialog_background)
        }

        lbBtn.setOnClickListener {
            kgBtn.isSelected = false
            lbBtn.isSelected = true
            val editor = sharedPreferences.edit()
            editor.putBoolean("useKg", kgBtn.isSelected)
            editor.apply()
            lbBtn.setBackgroundResource(R.drawable.exercise_background_pressed)
            kgBtn.setBackgroundResource(R.drawable.dialog_background)
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onExerciseSelected(exercise: Exercise) {
        selectedExercise = exercise
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete ${exercise.name}?")
            .setPositiveButton("Yes") { _, _ ->
                ExerciseRepository.removeExercise(this,exercise)
                updateExerciseListInDialog()
                Toast.makeText(this, "Exercise deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun loadSettings() {
        val useKg = sharedPreferences.getBoolean("useKg", true)
        kgBtn.isSelected = useKg
        lbBtn.isSelected = !useKg
        if (useKg){
            kgBtn.setBackgroundResource(R.drawable.exercise_background_pressed)
        }else{
            lbBtn.setBackgroundResource(R.drawable.exercise_background_pressed)
        }
    }

    private fun updateExerciseListInDialog() {
        val currentFragment = supportFragmentManager.findFragmentByTag("ExerciseDialogFragment")
        if (currentFragment is ExerciseDeleteDialogFragment) {
            currentFragment.updateExercises()
        }
    }
}
