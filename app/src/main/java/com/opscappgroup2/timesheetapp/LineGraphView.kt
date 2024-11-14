package com.opscappgroup2.timesheetapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LineGraphView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paintLine = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        isAntiAlias = true
    }
    private val paintPoint = Paint().apply {
        color = Color.RED
        strokeWidth = 12f
        isAntiAlias = true
    }
    private val paintGoal = Paint().apply {
        color = Color.GREEN
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private var dataPoints = listOf<Float>()
    private var minGoal = 0f
    private var maxGoal = 0f

    fun setData(points: List<Float>, minGoal: Float, maxGoal: Float) {
        dataPoints = points
        this.minGoal = minGoal
        this.maxGoal = maxGoal
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty()) return

        val graphWidth = width.toFloat()
        val graphHeight = height.toFloat()
        val maxDataPoint = dataPoints.maxOrNull() ?: 1f
        val scaleX = graphWidth / (dataPoints.size - 1)
        val scaleY = graphHeight / maxDataPoint

        // Draw goal lines
        val minGoalY = graphHeight - (minGoal * scaleY)
        val maxGoalY = graphHeight - (maxGoal * scaleY)
        canvas.drawLine(0f, minGoalY, graphWidth, minGoalY, paintGoal)
        canvas.drawLine(0f, maxGoalY, graphWidth, maxGoalY, paintGoal)

        // Draw data points and lines
        for (i in 0 until dataPoints.size - 1) {
            val startX = i * scaleX
            val startY = graphHeight - (dataPoints[i] * scaleY)
            val endX = (i + 1) * scaleX
            val endY = graphHeight - (dataPoints[i + 1] * scaleY)

            canvas.drawLine(startX, startY, endX, endY, paintLine)
            canvas.drawCircle(startX, startY, 8f, paintPoint)
        }

        // Draw last data point
        val lastX = (dataPoints.size - 1) * scaleX
        val lastY = graphHeight - (dataPoints.last() * scaleY)
        canvas.drawCircle(lastX, lastY, 8f, paintPoint)
    }
}