package com.example.workouttracker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp
import kotlin.math.pow

class StatsActivity : AppCompatActivity(), ExerciseSelectionListener {

    private lateinit var context: Context
    private lateinit var spinnerSet: Spinner
    private lateinit var spinnerPeriod: Spinner
    private lateinit var chart: LineChart
    private lateinit var btnBack: Button
    private lateinit var btnExercise: Button
    private var selectedExercise: Exercise? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        context = this

        btnExercise = findViewById(R.id.ExercisesBtn)
        spinnerSet = findViewById(R.id.spinner_set)
        spinnerPeriod = findViewById(R.id.spinner_period)
        chart = findViewById(R.id.chart)
        btnBack = findViewById(R.id.backBtn)

        setupSpinners()
        setupListeners()
        setupChart()
    }

    private fun setupSpinners() {
        val periods = resources.getStringArray(R.array.time_period)
        spinnerPeriod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periods)
        spinnerPeriod.setSelection(periods.indexOf("All Time"))

        val initialSetOptions = listOf("Choose Exercise")
        spinnerSet.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, initialSetOptions)
        spinnerSet.setSelection(0)


    }

    private fun setupListeners() {
        btnExercise.setOnClickListener {
            val dialogFragment = StatsExerciseDialogFragment()
            dialogFragment.show(supportFragmentManager, "StatsExerciseDialogFragment")
        }

        btnBack.setOnClickListener { finish() }

        spinnerSet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                chart.highlightValue(null)
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                chart.highlightValue(null)
                updateChart()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupChart() {
        chart.setNoDataText("Select an exercise to view stats")
        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.isEnabled = false
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.isScaleXEnabled = true
        chart.isScaleYEnabled = true
        chart.setPinchZoom(true)

        val marker = CustomMarkerView(this, R.layout.marker_view)
        marker.chartView = chart
        chart.marker = marker

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                chart.highlightValue(h)
            }

            override fun onNothingSelected() {
            }
        })

    }

    override fun onExerciseSelected(exercise: Exercise) {
        selectedExercise = exercise
        btnExercise.text = exercise.name

        val maxSets = getMaxSetsForExercise(exercise)

        val sets = (1..maxSets).map { "$it" }.toMutableList()
        sets.add(0, "All Sets")
        sets.add("Average Weight")
        sets.add("Maximal Weight")
        sets.add("Estimated 1RM")

        spinnerSet.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sets)
        spinnerSet.setSelection(0)
    }

    private fun getMaxSetsForExercise(exercise: Exercise): Int {
        val files = getRelevantFiles("All Time")
        var maxSets = 1

        for (file in files) {
            val lines = file.readLines().drop(1)
            val exerciseData = lines.filter { it.startsWith(exercise.name) }
            if (exerciseData.isNotEmpty()) {
                val setsCount = exerciseData.size
                if (setsCount > maxSets) {
                    maxSets = setsCount
                }
            }
        }

        return maxSets
    }

    private fun updateChart() {

        val allEntries = getChartData()

        if (allEntries.isEmpty() || allEntries.all { it.isEmpty() }) {
            chart.clear()
            if (selectedExercise == null){
                chart.setNoDataText("Select an exercise to view stats")
            }else{
                chart.setNoDataText("No data available for selected exercise")
            }
            chart.invalidate()
            return
        }

        val lineData = LineData()
        val colors = listOf(
            resources.getColor(R.color.colorSet1),
            resources.getColor(R.color.colorSet2),
            resources.getColor(R.color.colorSet3),
            resources.getColor(R.color.colorSet4),
            resources.getColor(R.color.colorSet5),
            resources.getColor(R.color.colorSet6),
            resources.getColor(R.color.colorSet7),
            resources.getColor(R.color.colorSet8),
            resources.getColor(R.color.colorSet9),
            resources.getColor(R.color.colorSet10)
        )

        allEntries.forEachIndexed { index, entries ->
            if (entries.isNotEmpty()) {
                val label: String = when (val spinnerText = spinnerSet.selectedItem.toString()) {
                    "Average Weight" -> "Average Weight"
                    "Maximal Weight" -> "Maximal Weight"
                    "Estimated 1RM" -> "Estimated 1RM"
                    "All Sets" -> {
                        val indexPlusOne = index + 1
                        when (indexPlusOne % 10) {
                            1 -> "${indexPlusOne}st Set"
                            2 -> "${indexPlusOne}nd Set"
                            3 -> "${indexPlusOne}rd Set"
                            else -> "${indexPlusOne}th Set"
                        }
                    }
                    else -> {
                        val spinnerInt = spinnerText.toIntOrNull() ?: 1
                        when (spinnerInt % 10) {
                            1 -> "${spinnerInt}st Set"
                            2 -> "${spinnerInt}nd Set"
                            3 -> "${spinnerInt}rd Set"
                            else -> "${spinnerInt}th Set"
                        }
                    }
                }

                val dataSet = LineDataSet(entries, label)
                dataSet.color = colors[index % colors.size]
                dataSet.valueTextSize = 10f
                dataSet.setDrawValues(true)
                dataSet.lineWidth = 2f
                dataSet.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f", value)
                    }
                }
                lineData.addDataSet(dataSet)
            }
        }

        chart.data = lineData

        chart.xAxis.valueFormatter = object : ValueFormatter() {
            private val dateFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
            init {
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            }
            override fun getFormattedValue(value: Float): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }


        chart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.2f", value)
            }
        }

        chart.invalidate()
    }

    private fun getChartData(): List<List<Entry>> {
        val allEntries = mutableListOf<MutableList<Entry>>()
        val selectedSetPosition = spinnerSet.selectedItemPosition
        val selectedPeriod = spinnerPeriod.selectedItem.toString()
        val selectedSet = spinnerSet.selectedItem.toString()

        val files = getRelevantFiles(selectedPeriod)

        val dataByDate = mutableMapOf<Date, MutableList<String>>()

        for (file in files) {
            val date = parseDate(file.name)
            val lines = file.readLines().drop(1)
            val exerciseData = lines.filter { it.startsWith(selectedExercise?.name ?: "") }

            if (exerciseData.isNotEmpty()) {
                dataByDate.getOrPut(date) { mutableListOf() }.addAll(exerciseData)
            }
        }

        when (selectedSet) {
            "All Sets" -> {
                for ((date, sets) in dataByDate) {
                    sets.forEachIndexed { index, set ->
                        val parts = set.split(",")
                        if (parts.size >= 4) {
                            val weight = parts[2].toFloatOrNull() ?: return@forEachIndexed
                            val reps = parts[3].toIntOrNull() ?: return@forEachIndexed

                            if (allEntries.size <= index) {
                                allEntries.add(mutableListOf())
                            }
                            if (reps > 0){
                                allEntries[index].add(Entry(date.time.toFloat(), weight, reps))
                            }

                        }
                    }
                }
            }
            "Average Weight" -> {
                val avgEntries = mutableListOf<Entry>()
                for ((date, sets) in dataByDate){
                    var totalWeight = 0f
                    var totalReps = 0
                    var setsCount = 0
                    var totalVolume = 0f
                    var volume: Float

                    for (set in sets) {
                        val parts = set.split(",")
                        if (parts.size >= 4) {
                            val weight = parts[2].toFloatOrNull() ?: continue
                            val reps = parts[3].toIntOrNull() ?: continue
                            if (reps > 0){
                                totalWeight += weight
                                totalReps += reps
                                setsCount++
                                volume = weight * reps
                                totalVolume += volume
                            }
                        }
                    }

                    if (setsCount > 0) {
                        val avgWeight = totalVolume / totalReps
                        val avgReps = Math.round(totalReps.toFloat() / setsCount)
                        avgEntries.add(Entry(date.time.toFloat(), avgWeight, avgReps))
                    }
                }
                allEntries.add(avgEntries)
            }
            "Maximal Weight" -> {
                val maxEntries = mutableListOf<Entry>()
                for ((date, sets) in dataByDate){
                    var maxWeight = 0f
                    var repsForMaxWeight = 0

                    for (set in sets) {
                        val parts = set.split(",")
                        if (parts.size >= 4) {
                            val weight = parts[2].toFloatOrNull() ?: continue
                            val reps = parts[3].toIntOrNull() ?: continue
                            if (reps > 0){
                                if (weight > maxWeight) {
                                    maxWeight = weight
                                    repsForMaxWeight = reps
                                }
                            }
                        }
                    }

                    if (maxWeight > 0) {
                        maxEntries.add(Entry(date.time.toFloat(), maxWeight, repsForMaxWeight))
                    }
                }
                allEntries.add(maxEntries)
            }
            "Estimated 1RM" -> {
                val estimated1RMEntries = mutableListOf<Entry>()
                for ((date, sets) in dataByDate){
                    var estimated1RM = 0f

                    for (set in sets) {
                        val parts = set.split(",")
                        if (parts.size >= 4) {
                            val weight = parts[2].toFloatOrNull() ?: continue
                            val reps = parts[3].toIntOrNull() ?: continue
                            if (reps > 0){
                                if(reps > 1){
                                    val epley = weight * (1 + reps / 30f)
                                    val brzycki = weight * (36/(37f-reps))
                                    val adams = weight * (1/(1-0.02*reps))
                                    val baechle = weight * (1 + 0.033*reps)
                                    val berger = weight * (1/(1.0261*exp(-0.0262*reps)))
                                    val brown = weight * (0.98489 + 0.0328*reps)
                                    val kemmler = weight * (0.988 + 0.0104*reps + 0.00190 * reps * reps - 0.0000584 * reps * reps * reps)
                                    val landers = weight * (1/(1.013 - 0.0267123*reps))
                                    val lombardi = weight * reps.toDouble().pow(0.1)
                                    val mayhew = weight * (1/(0.522+0.419*exp(-0.055*reps)))
                                    val naclerio = weight * (1/(0.951*exp(-0.021*reps)))
                                    val oconner = weight * (1 + 0.025*reps)
                                    val wathen = weight * (1/(0.4880 + 0.538 * exp(-0.075*reps)))
                                    val sumEstimated1RM  = (epley + brzycki + adams + baechle + berger + brown + kemmler + landers + lombardi + mayhew + naclerio + oconner + wathen).toFloat()
                                    val estimated1RMOfSet = sumEstimated1RM / 13

                                    if (estimated1RMOfSet > estimated1RM) {
                                        estimated1RM = estimated1RMOfSet
                                    }
                                }else{
                                    estimated1RM = weight
                                }
                            }
                        }
                    }
                    if (estimated1RM > 0){
                        estimated1RMEntries.add(Entry(date.time.toFloat(), estimated1RM, 1))
                    }

                }
                allEntries.add(estimated1RMEntries)
            }
            else -> {
                val setEntries = mutableListOf<Entry>()
                for ((date, sets) in dataByDate){
                    if (selectedSetPosition >= 1 && selectedSetPosition <= sets.size) {
                        val set = sets[selectedSetPosition - 1]
                        val parts = set.split(",")
                        if (parts.size >= 4) {
                            val weight = parts[2].toFloatOrNull() ?: continue
                            val reps = parts[3].toIntOrNull() ?: continue
                            if (reps > 0){
                                setEntries.add(Entry(date.time.toFloat(), weight, reps))
                            }
                        }
                    }
                }
                allEntries.add(setEntries)
            }
        }
        val prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val useKg = prefs.getBoolean("useKg", true)

        val allEntriesConvertedToLB = allEntries.map { entries ->
            entries.map { entry ->
                Entry(entry.x, (entry.y * 2.2046226218487757).toFloat(), entry.data)
            }.toMutableList()
        }.toMutableList()

        return if (useKg){
            allEntries.map { it.sortedBy { entry -> entry.x } }
        }else{
            allEntriesConvertedToLB.map { it.sortedBy { entry -> entry.x } }
        }

    }

    private fun getRelevantFiles(period: String): List<File> {
        val dir = File(getExternalFilesDir(null), "Documents")
        val startDate = Calendar.getInstance()

        when (period) {
            "1 Week" -> startDate.add(Calendar.DAY_OF_YEAR, -7)
            "2 Weeks" -> startDate.add(Calendar.DAY_OF_YEAR, -14)
            "1 Month" -> startDate.add(Calendar.MONTH, -1)
            "3 Months" -> startDate.add(Calendar.MONTH, -3)
            "6 Months" -> startDate.add(Calendar.MONTH, -6)
            "Year" -> startDate.add(Calendar.YEAR, -1)
            "All Time" -> startDate.timeInMillis = 0
        }

        return dir.listFiles { file ->
            file.isFile && file.name.endsWith(".csv") && parseDate(file.name).after(startDate.time)
        }?.sortedBy { it.name } ?: emptyList()
    }

    private fun parseDate(fileName: String): Date {
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return try {
            dateFormat.parse(fileName.substring(0, 8)) ?: Date()
        } catch (e: Exception) {
            Log.e("StatsActivity", "Error parsing date from filename: $fileName", e)
            Date()
        }
    }

    fun getSavedExercises(): List<String> {
        val files = getRelevantFiles("All Time")
        val exerciseNames = mutableSetOf<String>()

        for (file in files) {
            val lines = file.readLines().drop(1)
            lines.forEach { line ->
                val exerciseName = line.split(",").firstOrNull() ?: ""
                if (exerciseName.isNotEmpty()) {
                    exerciseNames.add(exerciseName)
                }
            }
        }

        return exerciseNames.toList()
    }

}