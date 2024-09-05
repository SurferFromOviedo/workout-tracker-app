package com.example.workouttracker

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ExerciseDialogFragment : DialogFragment(), ExerciseAdapter.ExerciseClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var spinnerBodyPart: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var searchEditText: EditText
    private lateinit var exerciseUsageManager: ExerciseUsageManager
    private lateinit var allExercises: List<Exercise>
    private lateinit var filteredExercises: List<Exercise>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseUsageManager = ExerciseUsageManager(requireContext())
        allExercises = ExerciseRepository.getExercises()
        filteredExercises = allExercises
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise_dialog, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewExercises)
        spinnerBodyPart = view.findViewById(R.id.spinnerBodyPart)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        searchEditText = view.findViewById(R.id.searchExercises)

        setupSearchEditText()
        setupSpinners()
        setupRecyclerView()

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setDimAmount(0.8f)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog ?: return
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (resources.displayMetrics.widthPixels * 1.5).toInt()
        dialog.window?.setLayout(width, height)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }

    private fun setupSearchEditText() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterExercises()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.body_parts,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerBodyPart.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter
        }

        spinnerBodyPart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterExercises()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterExercises()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val usages = exerciseUsageManager.getExerciseUsages()
        val sortedExercises = filteredExercises.sortedByDescending { exercise ->
            usages.find { it.name == exercise.name }?.count ?: 0
        }
        val adapter = ExerciseAdapter(sortedExercises, this)
        recyclerView.adapter = adapter
    }

    override fun onExerciseClicked(exercise: Exercise) {
        (requireActivity() as? ExerciseSelectionListener)?.onExerciseSelected(exercise)
        dismiss()
    }

    private fun filterExercises() {
        val selectedBodyPart = spinnerBodyPart.selectedItem.toString()
        val selectedCategory = spinnerCategory.selectedItem.toString()
        val searchQuery = searchEditText.text.toString().lowercase(Locale.getDefault())
        val usages = exerciseUsageManager.getExerciseUsages()

        filteredExercises = allExercises.filter { exercise ->
            (selectedBodyPart == "Any Body Part" || exercise.bodypart == selectedBodyPart) &&
                    (selectedCategory == "Any Category" || exercise.category == selectedCategory) &&
                    exercise.name.lowercase(Locale.getDefault()).contains(searchQuery)
        }

        val sortedExercises = filteredExercises.sortedByDescending { exercise ->
            usages.find { it.name == exercise.name }?.count ?: 0
        }

        (recyclerView.adapter as? ExerciseAdapter)?.updateExercises(sortedExercises)
    }
}
