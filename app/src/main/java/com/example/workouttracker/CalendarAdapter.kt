package com.example.workouttracker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private var year: Int, private val context: Context) : RecyclerView.Adapter<CalendarAdapter.MonthViewHolder>() {

    private val workouts = mutableMapOf<String, List<List<String>>>()

    init {
        loadWorkouts()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_month, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(year, position, 1)

        val monthYearFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        holder.tvMonthName.text = monthYearFormat.format(tempCalendar.time)

        holder.gridCalendarDays.removeAllViews()

        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfMonth = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1

        val emptyDays = (firstDayOfMonth - Calendar.SUNDAY + 7) % 7
        for (i in 0 until emptyDays) {
            val emptyView = TextView(holder.itemView.context)
            holder.gridCalendarDays.addView(emptyView, GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            })
        }

        for (day in 1..daysInMonth) {
            val dayView = FrameLayout(holder.itemView.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                val textView = TextView(context).apply {
                    text = day.toString()
                    textSize = 16f
                    gravity = Gravity.CENTER
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
                addView(textView)

                val dateKey = String.format(Locale.getDefault(), "%02d%02d%04d", day, position + 1, year)
                if (workouts.containsKey(dateKey)) {
                    textView.setTypeface(textView.typeface, android.graphics.Typeface.BOLD)
                    textView.setTextColor(Color.BLACK)
                    addWorkoutIndicators(this, dateKey)
                    setOnClickListener {
                        launchWorkoutActivity(dateKey)
                    }
                }
            }

            val layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            holder.gridCalendarDays.addView(dayView, layoutParams)
        }
    }

    override fun getItemCount(): Int = 12

    fun updateYear(newYear: Int) {
        year = newYear
        notifyDataSetChanged()
    }

    private fun loadWorkouts() {
        CoroutineScope(Dispatchers.IO).launch {
            val workoutMap = mutableMapOf<String, MutableList<List<String>>>()
            val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return@launch
            filesDir.listFiles()?.forEach { file ->
                if (file.extension == "csv") {
                    val dateKey = file.nameWithoutExtension.substringBefore("_")
                    val exercises = parseExercises(file)
                    workoutMap.getOrPut(dateKey) { mutableListOf() }.add(exercises)
                }
            }
            withContext(Dispatchers.Main) {
                workouts.putAll(workoutMap)
                notifyDataSetChanged()
            }
        }
    }

    private fun parseExercises(file: File): List<String> {
        return file.readLines()
            .drop(1)
            .map { line -> line.split(",").firstOrNull() ?: "" }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun addWorkoutIndicators(container: FrameLayout, dateKey: String) {
        val allExercises = workouts[dateKey]?.flatten() ?: return
        val bodyParts = allExercises.asSequence().map { ExerciseRepository.getBodyPartForExercise(it) }
            .distinct()
            .map {
                if (it == "Biceps" || it == "Triceps") "Arms" else it
            }
            .distinct()
            .filter { getWorkoutIndicatorDrawable(it) != null }.toList()

        val indicatorSize = context.resources.getDimensionPixelSize(R.dimen.workout_indicator_size)
        val margin = context.resources.getDimensionPixelSize(R.dimen.workout_indicator_margin)

        container.post {
            val totalWidth = bodyParts.size * indicatorSize + (bodyParts.size - 1) * margin
            val startX = (container.width - totalWidth) / 2
            val rightShift = 13.5

            bodyParts.forEachIndexed { index, bodyPart ->
                val indicator = View(context).apply {
                    layoutParams = FrameLayout.LayoutParams(indicatorSize, indicatorSize).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                        bottomMargin = margin
                        leftMargin = (startX + index * (indicatorSize + margin) - container.width / 2 + rightShift).toInt()
                    }
                    background = ContextCompat.getDrawable(context, getWorkoutIndicatorDrawable(bodyPart) ?: return@forEachIndexed)
                }
                container.addView(indicator)
            }
        }
    }

    private fun getWorkoutIndicatorDrawable(bodyPart: String): Int? {
        return when (bodyPart) {
            "Legs" -> R.drawable.indicator_legs
            "Back" -> R.drawable.indicator_back
            "Chest" -> R.drawable.indicator_chest
            "Shoulders" -> R.drawable.indicator_shoulders
            "Arms" -> R.drawable.indicator_arms
            "Core" -> R.drawable.indicator_core
            "Forearms" -> R.drawable.indicator_forearms
            "Whole Body" -> R.drawable.indicator_whole_body
            else -> null
        }
    }

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMonthName: TextView = itemView.findViewById(R.id.tvMonthName)
        val gridCalendarDays: GridLayout = itemView.findViewById(R.id.gridCalendarDays)
    }

    fun refreshData() {
        workouts.clear()
        loadWorkouts()
    }

    private fun launchWorkoutActivity(dateKey: String) {
        val workoutsForDay = workouts[dateKey] ?: return

        if (workoutsForDay.size == 1) {
            val filePath = getWorkoutFilePath(dateKey)
            val intent = Intent(context, WorkoutDetailedActivity::class.java).apply {
                putExtra("WORKOUT_FILE_NAME", filePath)
            }
            (context as? CalendarActivity)?.startActivityForResult(intent, CalendarActivity.WORKOUT_REQUEST_CODE)
        } else {
            val intent = Intent(context, WorkoutActivity::class.java).apply {
                putExtra("date", dateKey)
            }
            (context as? CalendarActivity)?.startActivityForResult(intent, CalendarActivity.WORKOUT_REQUEST_CODE)
        }
    }

    private fun getWorkoutFilePath(dateKey: String): String {
        val filesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val matchingFiles = filesDir?.listFiles { _, name ->
            name.startsWith(dateKey)
        } ?: emptyArray()

        return if (matchingFiles.size == 1) {
            matchingFiles[0].absolutePath
        } else {
            ""
        }
    }

}








