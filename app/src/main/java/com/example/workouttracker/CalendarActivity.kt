package com.example.workouttracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {

    private lateinit var recyclerViewCalendar: RecyclerView
    private lateinit var tvYear: TextView
    private lateinit var btnPreviousYear: Button
    private lateinit var btnNextYear: Button
    private lateinit var btnBack: Button
    private lateinit var adapter: CalendarAdapter
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    private lateinit var legendLayout: ConstraintLayout

    companion object {
        const val WORKOUT_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        recyclerViewCalendar = findViewById(R.id.recyclerViewCalendar)
        tvYear = findViewById(R.id.tvYear)
        btnPreviousYear = findViewById(R.id.btnPreviousYear)
        btnNextYear = findViewById(R.id.btnNextYear)
        btnBack = findViewById(R.id.btnBack)

        adapter = CalendarAdapter(currentYear, this)
        recyclerViewCalendar.adapter = adapter
        recyclerViewCalendar.layoutManager = LinearLayoutManager(this)

        recyclerViewCalendar.scrollToPosition(currentMonth)
        tvYear.text = currentYear.toString()

        btnPreviousYear.setOnClickListener {
            currentYear--
            tvYear.text = currentYear.toString()
            adapter.updateYear(currentYear)
        }

        btnNextYear.setOnClickListener {
            currentYear++
            tvYear.text = currentYear.toString()
            adapter.updateYear(currentYear)
        }

        btnBack.setOnClickListener {
            finish()
        }

        legendLayout = findViewById(R.id.legendLayout)

        setupLegend()
    }
    private fun setupLegend() {
        val legendItems = listOf(
            Pair("Legs", R.drawable.indicator_legs),
            Pair("Back", R.drawable.indicator_back),
            Pair("Chest", R.drawable.indicator_chest),
            Pair("Shoulders", R.drawable.indicator_shoulders),
            Pair("Arms", R.drawable.indicator_arms),
            Pair("Core", R.drawable.indicator_core),
            Pair("Forearms", R.drawable.indicator_forearms),
            Pair("Whole Body", R.drawable.indicator_whole_body)
        )

        val itemMargin = resources.getDimensionPixelSize(R.dimen.legend_item_margin)
        val screenWidth = resources.displayMetrics.widthPixels
        val totalMargins = itemMargin * (legendItems.size + 1)
        val itemWidth = (screenWidth - totalMargins) / legendItems.size

        val chainRefs = mutableListOf<Int>()

        legendItems.forEachIndexed { _, (text, drawableRes) ->
            val itemView = layoutInflater.inflate(R.layout.legend_item, legendLayout, false)
            val indicatorView = itemView.findViewById<View>(R.id.legendIndicator)
            val textView = itemView.findViewById<TextView>(R.id.legendText)

            indicatorView.background = ContextCompat.getDrawable(this, drawableRes)
            textView.text = text

            val params = ConstraintLayout.LayoutParams(itemWidth, ConstraintLayout.LayoutParams.WRAP_CONTENT)

            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

            itemView.id = View.generateViewId()
            legendLayout.addView(itemView, params)

            chainRefs.add(itemView.id)
        }

        ConstraintSet().apply {
            clone(legendLayout)
            createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                chainRefs.toIntArray(),
                null,
                ConstraintSet.CHAIN_SPREAD
            )
            applyTo(legendLayout)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == WORKOUT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshCalendarData()
        }
    }

    private fun refreshCalendarData() {
        (recyclerViewCalendar.adapter as? CalendarAdapter)?.refreshData()
    }
}






