package com.example.workouttracker

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectedExerciseAdapter(
    private val context: Context,
    private val selectedExercises: List<Exercise>,
    private val listener: OnExerciseClickListener
) : RecyclerView.Adapter<SelectedExerciseAdapter.SelectedExerciseViewHolder>() {

    interface OnExerciseClickListener {
        fun onExerciseClicked(exerciseId: String)
    }

    private var selectedExerciseId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_exercise, parent, false)
        return SelectedExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedExerciseViewHolder, position: Int) {
        val exercise = selectedExercises[position]
        holder.exerciseName.text = exercise.name

        if (exercise.id == selectedExerciseId) {
            holder.itemView.setBackgroundResource(R.drawable.exercise_background_pressed)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.dialog_background)
        }

        holder.itemView.setOnClickListener {
            listener.onExerciseClicked(exercise.id)
            setSelectedExerciseId(exercise.id)
        }

        holder.setsContainer.removeAllViews()
        for (set in exercise.sets) {
            addSetView(holder.setsContainer, set)
        }
    }

    override fun getItemCount(): Int {
        return selectedExercises.size
    }

    private fun addSetView(setsContainer: LinearLayout, set: Set) {
        val setView = LayoutInflater.from(setsContainer.context)
            .inflate(R.layout.item_set, setsContainer, false)

        val weightEditText: EditText = setView.findViewById(R.id.weightEditText)
        val repsEditText: EditText = setView.findViewById(R.id.repsEditText)
        val unitToggleButton: Button = setView.findViewById(R.id.unitToggleButton)

        val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val useKg = prefs.getBoolean("useKg", true)
        if (set.unit.isEmpty()) {
            set.unit = if (useKg) "KG" else "LB"
        }

        weightEditText.setText(set.weight)
        repsEditText.setText(set.reps)
        unitToggleButton.text = set.unit


        unitToggleButton.setOnClickListener {
            set.unit = if (set.unit == "KG") "LB" else "KG"
            unitToggleButton.text = set.unit
        }

        weightEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.weight = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        repsEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                set.reps = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        setsContainer.addView(setView)
    }

    fun setSelectedExerciseId(id: String?) {
        selectedExerciseId = id
        notifyDataSetChanged()
    }

    class SelectedExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val exerciseName: TextView = itemView.findViewById(R.id.exerciseName)
        val setsContainer: LinearLayout = itemView.findViewById(R.id.setsContainer)
    }
}














