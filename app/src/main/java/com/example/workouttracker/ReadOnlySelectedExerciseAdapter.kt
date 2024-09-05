package com.example.workouttracker

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.math.BigDecimal
import java.math.RoundingMode

class ReadOnlySelectedExerciseAdapter(private val context: Context,
    private val selectedExercises: List<Exercise>
) : RecyclerView.Adapter<ReadOnlySelectedExerciseAdapter.ReadOnlySelectedExerciseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadOnlySelectedExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_exercise, parent, false)
        return ReadOnlySelectedExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReadOnlySelectedExerciseViewHolder, position: Int) {
        val exercise = selectedExercises[position]
        holder.exerciseName.text = exercise.name

        holder.setsContainer.removeAllViews()
        for (set in exercise.sets) {
            addReadOnlySetView(holder.setsContainer, set)
        }
    }

    override fun getItemCount(): Int {
        return selectedExercises.size
    }

    private fun addReadOnlySetView(setsContainer: LinearLayout, set: Set) {

        val setView = LayoutInflater.from(setsContainer.context)
            .inflate(R.layout.item_set, setsContainer, false)

        val weightTextView: TextView = setView.findViewById(R.id.weightEditText)
        val repsTextView: TextView = setView.findViewById(R.id.repsEditText)
        val unitToggleButton: TextView = setView.findViewById(R.id.unitToggleButton)

        weightTextView.isEnabled = false
        repsTextView.isEnabled = false

        fun formatWeight(weight: Double): String {
            return if (weight == weight.toInt().toDouble()) {
                weight.toInt().toString()
            } else {
                String.format("%.2f", weight).trimEnd('0').trimEnd('.')
            }
        }
        val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val useKg = prefs.getBoolean("useKg", true)

        if (!useKg){
            val weight = set.weight.toFloatOrNull() ?: 0f
            set.weight = (weight * 2.2046226218487757).toString()
        }

        weightTextView.text = formatWeight(set.weight.toDouble())
        repsTextView.text = set.reps

        weightTextView.setTextColor(Color.BLACK)
        repsTextView.setTextColor(Color.BLACK)

        if(useKg){
            set.unit = "KG"
        }else{
            set.unit = "LB"
        }

        unitToggleButton.text = set.unit
        unitToggleButton.setOnClickListener {
            val currentWeight = set.weight.toFloatOrNull() ?: 0f
            val convertedWeight = if (set.unit == "KG") {
                // Convert KG to LB
                BigDecimal(currentWeight * 2.2046226218487757)
                    .setScale(3, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            } else {
                BigDecimal(currentWeight / 2.2046226218487757)
                    .setScale(3, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }

            set.weight = convertedWeight
            set.unit = if (set.unit == "KG") "LB" else "KG"

            weightTextView.text = formatWeight(convertedWeight.toDouble())
            unitToggleButton.text = set.unit
        }

        setsContainer.addView(setView)
    }

    class ReadOnlySelectedExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        val setsContainer: LinearLayout = itemView.findViewById(R.id.setsContainer)
    }
}