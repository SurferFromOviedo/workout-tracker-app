package com.example.workouttracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.workouttracker.ExerciseRepository.getBodyPartForExercise
import java.io.File

class WorkoutAdapter(
    private val workouts: MutableList<Triple<String, List<Workout>, String>>,
    private val listener: WorkoutClickListener
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    interface WorkoutClickListener {
        fun onWorkoutClicked(position: Int, filePath: String)
        fun onWorkoutLongClicked(position: Int)
    }

    private var selectedWorkoutPosition: Int = -1
    private val selectedWorkouts = mutableSetOf<Int>()
    private var isMultiSelectMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout2, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val (workoutTitle, workoutList, filePath) = workouts[position]
        holder.tvWorkoutTitle.text = workoutTitle

        val duration = getDurationFromFile(filePath)
        val bodyParts = workoutList.map { getBodyPartForExercise(it.exercise) }.toSet().joinToString()
        val exercises = workoutList.joinToString { it.exercise }

        holder.tvWorkoutDuration.text = "Duration: $duration"
        holder.tvWorkoutBodyParts.text = "Body Parts: $bodyParts"
        holder.tvWorkoutExercises.text = "Exercises: $exercises"

        updateItemBackground(holder, position)

        holder.itemView.setOnClickListener {
            listener.onWorkoutClicked(position, filePath)
        }

        holder.itemView.setOnLongClickListener {
            listener.onWorkoutLongClicked(position)
            true
        }

        updateItemBackground(holder, position)
    }

    private fun updateItemBackground(holder: WorkoutViewHolder, position: Int) {
        if (selectedWorkouts.contains(position)) {
            holder.itemView.setBackgroundResource(R.drawable.exercise_background_pressed)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.dialog_background)
        }
    }

    fun toggleWorkoutSelection(position: Int) {
        if (selectedWorkouts.contains(position)) {
            selectedWorkouts.remove(position)
        } else {
            selectedWorkouts.add(position)
        }
        notifyItemChanged(position)
    }

    fun enterMultiSelectMode() {
        isMultiSelectMode = true
        notifyDataSetChanged()
    }

    fun exitMultiSelectMode() {
        isMultiSelectMode = false
        selectedWorkouts.clear()
        notifyDataSetChanged()
    }

    fun getSelectedWorkoutsCount() = selectedWorkouts.size

    fun getSelectedWorkouts(): List<Triple<String, List<Workout>, String>> {
        return selectedWorkouts.map { workouts[it] }
    }

    fun removeSelectedWorkouts() {
        val sortedPositions = selectedWorkouts.sortedDescending()
        sortedPositions.forEach { position ->
            workouts.removeAt(position)
        }
        selectedWorkouts.clear()
        notifyDataSetChanged()
    }

    fun clearSelections() {
        selectedWorkouts.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = workouts.size

    private fun getDurationFromFile(filePath: String): String {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val lines = file.readLines()
                val durationLine = lines.find { it.startsWith("Total Duration") }
                durationLine?.split(",")?.getOrNull(1)?.trim() ?: "00:00:00"
            } else {
                "00:00:00"
            }
        } catch (e: Exception) {
            "00:00:00"
        }
    }


    fun setSelectedWorkoutPosition(position: Int) {
        val previousPosition = selectedWorkoutPosition
        selectedWorkoutPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(position)
    }

    fun getSelectedWorkoutPosition(): Int = selectedWorkoutPosition

    fun setWorkouts(newWorkouts: List<Triple<String, List<Workout>, String>>) {
        workouts.clear()
        workouts.addAll(newWorkouts)
        notifyDataSetChanged()
    }

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWorkoutTitle: TextView = itemView.findViewById(R.id.tvWorkoutTitle)
        val tvWorkoutDuration: TextView = itemView.findViewById(R.id.tvWorkoutDuration)
        val tvWorkoutBodyParts: TextView = itemView.findViewById(R.id.tvWorkoutBodyParts)
        val tvWorkoutExercises: TextView = itemView.findViewById(R.id.tvWorkoutExercises)
    }
}








