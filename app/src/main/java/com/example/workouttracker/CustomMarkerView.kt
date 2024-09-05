package com.example.workouttracker

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvDate: TextView = findViewById(R.id.tvDate)
    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry, highlight: Highlight) {
        val date = Date(e.x.toLong())
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(date)

        val sb = StringBuilder()

        for (dataSet in (chartView as LineChart).data.dataSets) {
            val entry = dataSet.getEntryForXValue(e.x, e.y)
            if (entry != null && entry.x == e.x) {
                sb.append("${dataSet.label}: ${String.format("%.2f", entry.y)} (${entry.data} ${if (entry.data == 1) "rep" else "reps"})\n")
            }
        }

        tvContent.text = sb.toString().trimEnd()
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}
