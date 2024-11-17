package com.opscappgroup2.timesheetapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi

class BarChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paintBar = Paint().apply {
        color = Color.parseColor("#D8BFD8") // Light purple with a hint of white
        isAntiAlias = true
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private val paintText = Paint().apply {
        color = Color.parseColor("#3C3C3C") // Custom text color
        textSize = 32f
        isAntiAlias = true
        typeface = resources.getFont(R.font.custom_font)
    }
    private val paintYAxis = Paint().apply {
        color = Color.parseColor("#555555") // Darker line color
        strokeWidth = 3f
        isAntiAlias = true
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private val paintYAxisLabel = Paint().apply {
        color = Color.parseColor("#3C3C3C")
        textSize = 36f
        isAntiAlias = true
        typeface = resources.getFont(R.font.custom_font)
    }

    private var dataPoints = listOf<Float>()
    private var dateLabels = listOf<String>()

    fun setData(points: List<Float>, labels: List<String>) {
        dataPoints = points
        dateLabels = labels
        invalidate() // Redraw the view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty() || dateLabels.isEmpty()) return

        val barWidth = width / (dataPoints.size * 2)
        val leftPadding = 20f
        val topPadding = 100f
        val maxHeight = height - topPadding - 50f
        val maxDataPoint = 14f // Y-axis range from 0 to 14

        // Draw "Hours" label above the Y-axis numbers
        canvas.drawText("Hours", 60f, topPadding - 20f, paintYAxisLabel)

        // Draw Y-axis labels (0 to 14 with intervals of 2)
        for (i in 0..7) {
            val y = maxHeight - (i * maxHeight / 7) + topPadding
            canvas.drawLine(leftPadding, y, width.toFloat(), y, paintYAxis)
            canvas.drawText("${i * 2}", 0f, y + 10f, paintText)
        }

        // Draw bars and date labels
        for (i in dataPoints.indices) {
            val barHeight = (dataPoints[i] / maxDataPoint) * maxHeight
            val left = i * 2 * barWidth + barWidth / 2 + leftPadding
            val top = height - barHeight - 50f
            val right = left + barWidth
            val bottom = height - 50f

            // Draw the bar
            canvas.drawRect(left.toFloat(), top, right.toFloat(), bottom, paintBar)

            // Draw the date label
            canvas.drawText(dateLabels[i], left.toFloat(), height - 10f, paintText)
        }
    }
}