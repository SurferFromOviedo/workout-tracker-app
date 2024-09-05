package com.example.workouttracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.os.Looper
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private lateinit var handler: Handler
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeExercises()

        val sharedPreferences = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        if (!sharedPreferences.contains("useKg")) {
            sharedPreferences.edit().putBoolean("useKg", true).apply()
        }

        timeTextView = findViewById(R.id.tw_date_time)
        handler = Handler(Looper.getMainLooper())

        startUpdatingTime()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnStart = findViewById(R.id.btnStart)
        updateStartButtonText()
        btnStart.setOnClickListener {
            startActivity(Intent(this, StartActivity::class.java))
        }

        findViewById<Button>(R.id.btnCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        findViewById<Button>(R.id.btnStats).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun initializeExercises() {
        ExerciseRepository.loadExercisesFromSharedPreferences(this)
        if (ExerciseRepository.getExercises().isEmpty()) {
            ExerciseRepository.initializeDefaultExercises()
            ExerciseRepository.saveExercisesToSharedPreferences(this)
        }
    }

    private fun updateStartButtonText() {
        btnStart.text = if (hasSavedWorkout()) "Continue" else "Start"
    }

    override fun onResume() {
        super.onResume()
        updateStartButtonText()
    }

    private fun startUpdatingTime() {
        handler.post(object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun updateTime() {
        val sdf = SimpleDateFormat("dd/MM/yyyy \n   HH:mm:ss", Locale.getDefault())
        timeTextView.text = sdf.format(Date())
    }

    private fun hasSavedWorkout(): Boolean {
        return getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE)
            .getString("exercises", null) != null
    }
}