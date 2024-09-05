package com.example.workouttracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExerciseAdapter(
    private var exercises: List<Exercise>,
    private val exerciseClickListener: ExerciseClickListener
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    interface ExerciseClickListener {
        fun onExerciseClicked(exercise: Exercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
        holder.itemView.setOnClickListener {
            exerciseClickListener.onExerciseClicked(exercise)
        }
    }

    override fun getItemCount(): Int {
        return exercises.size
    }

    fun updateExercises(newExercises: List<Exercise>) {
        exercises = newExercises
        notifyDataSetChanged()
    }

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val bodypartTextView: TextView = itemView.findViewById(R.id.bodypartTextView)
        private val categoryTextView: TextView = itemView.findViewById(R.id.categoryTextView)

        fun bind(exercise: Exercise) {
            nameTextView.text = exercise.name
            bodypartTextView.text = exercise.bodypart
            categoryTextView.text = exercise.category
        }
    }
}





