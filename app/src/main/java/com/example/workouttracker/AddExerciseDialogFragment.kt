package com.example.workouttracker

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class AddExerciseDialogFragment : DialogFragment() {

    private lateinit var exerciseNameEditText: EditText
    private lateinit var bodyPartSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var saveExerciseButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setDimAmount(0.8f)
        return inflater.inflate(R.layout.dialog_add_exercise, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawableResource(android.R.color.transparent)

            val params = attributes
            params.x = 0
            params.y = 0
            params.gravity = Gravity.CENTER
            attributes = params
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exerciseNameEditText = view.findViewById(R.id.exerciseNameEditText)
        bodyPartSpinner = view.findViewById(R.id.bodyPartSpinner)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        saveExerciseButton = view.findViewById(R.id.saveExerciseButton)

        setupSpinners()
        setupSaveButton()
    }

    private fun setupSpinners() {
        val bodyParts = resources.getStringArray(R.array.body_parts)
        val bodyPartsWithoutFirst = bodyParts.drop(1)
        val bodyPartsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            bodyPartsWithoutFirst
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        bodyPartSpinner.adapter = bodyPartsAdapter

        val categoriesArray = resources.getStringArray(R.array.categories)
        val categoriesArrayWithoutFirst = categoriesArray.drop(1)
        val categoriesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoriesArrayWithoutFirst
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        categorySpinner.adapter = categoriesAdapter
    }

    private fun setupSaveButton() {
        saveExerciseButton.setOnClickListener {
            val exerciseName = exerciseNameEditText.text.toString().trim()
            val bodyPart = bodyPartSpinner.selectedItem.toString()
            val category = categorySpinner.selectedItem.toString()

            if (exerciseName.isNotEmpty()) {
                val existingExercise = ExerciseRepository.getExercises().find {
                    it.name.equals(exerciseName, ignoreCase = true)
                }
                if (existingExercise != null) {
                    Toast.makeText(requireContext(), "Exercise with this name already exists", Toast.LENGTH_SHORT).show()
                } else {
                    val newExercise = Exercise(exerciseName, bodyPart, category)
                    ExerciseRepository.addExercise(requireContext(), newExercise)
                    Toast.makeText(requireContext(), "Exercise added", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            } else {
                exerciseNameEditText.error = "Please enter exercise name"
            }
        }
    }
}