package com.opscappgroup2.timesheetapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class HoursActivity : AppCompatActivity() {

    private lateinit var minGoalEditText: EditText
    private lateinit var maxGoalEditText: EditText
    private lateinit var saveGoalsButton: Button
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var viewReportButton: Button
    private lateinit var backToNavigationButton: Button
    private lateinit var totalHoursTextView: TextView
    private lateinit var showHoursGraphButton: Button
    private lateinit var showGoalPerformanceButton: Button

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hours)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: return

        minGoalEditText = findViewById(R.id.minGoalEditText)
        maxGoalEditText = findViewById(R.id.maxGoalEditText)
        saveGoalsButton = findViewById(R.id.saveGoalsButton)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        viewReportButton = findViewById(R.id.viewReportButton)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)
        totalHoursTextView = findViewById(R.id.totalHoursTextView)
        showHoursGraphButton = findViewById(R.id.showHoursGraphButton)
        showGoalPerformanceButton = findViewById(R.id.showGoalPerformanceButton)

        loadGoals()

        saveGoalsButton.setOnClickListener { saveGoals() }
        backToNavigationButton.setOnClickListener { finish() }


        startDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                startDate = date
                startDateButton.text = date.toFormattedString()
            }
        }

        endDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                endDate = date
                endDateButton.text = date.toFormattedString()
            }
        }

        viewReportButton.setOnClickListener {
            if (startDate != null && endDate != null) {
                // Check if startDate is before or equal to endDate
                if (startDate!!.after(endDate!!)) {
                    Toast.makeText(this, "Start date must be before or equal to end date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Call the updated calculateTotalHoursWorked function
                calculateTotalHoursWorked(startDate!!, endDate!!) { dataPoints ->
                    if (dataPoints.isNotEmpty()) {
                        // Calculate the total hours worked by summing the list of Float values
                        val totalHoursWorked = dataPoints.sum()
                        totalHoursTextView.text = "Total Hours Worked: %.2f hrs".format(totalHoursWorked)

                        // Display a toast message with the result
                        Toast.makeText(this, "Total hours worked calculated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        totalHoursTextView.text = "Total Hours Worked: 0 hrs"
                        Toast.makeText(this, "No data points found for the selected period.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            }
        }
        showHoursGraphButton.setOnClickListener {
            showGraphPopup()
        }

        showGoalPerformanceButton.setOnClickListener {
            showGoalPerformancePopup()
        }
    }

    private fun loadGoals() {
        val goalsRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("hourGoals")

        goalsRef.get().addOnSuccessListener { snapshot ->
            val minGoal = snapshot.child("minGoal").getValue(Float::class.java) ?: 0f
            val maxGoal = snapshot.child("maxGoal").getValue(Float::class.java) ?: 0f

            if (minGoal > 0) minGoalEditText.setText(minGoal.toString())
            if (maxGoal > 0) maxGoalEditText.setText(maxGoal.toString())
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load goals: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGoals() {
        val minGoal = minGoalEditText.text.toString().toFloatOrNull()
        val maxGoal = maxGoalEditText.text.toString().toFloatOrNull()

        if (minGoal == null || maxGoal == null) {
            Toast.makeText(this, "Please enter valid goals", Toast.LENGTH_SHORT).show()
            return
        }
        if (minGoal > maxGoal) {
            Toast.makeText(this, "Min goal cannot be greater than max goal", Toast.LENGTH_SHORT).show()
            return
        }

        val goalsRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("hourGoals")
        val goalsMap = mapOf("minGoal" to minGoal, "maxGoal" to maxGoal)

        goalsRef.setValue(goalsMap).addOnSuccessListener {
            Toast.makeText(this, "Goals saved to Firebase.", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to save goals: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            onDateSet(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun calculateTotalHoursWorked(startDate: Calendar, endDate: Calendar, onResult: (List<Float>) -> Unit) {
        val categoriesRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("categories")
        val dataPoints = mutableListOf<Float>()

        categoriesRef.get().addOnSuccessListener { categoriesSnapshot ->
            if (categoriesSnapshot.exists()) {
                categoriesSnapshot.children.forEach { categorySnapshot ->
                    val timesheetsRef = categorySnapshot.child("timesheets")

                    timesheetsRef.children.forEach { timesheetSnapshot ->
                        val timesheet = timesheetSnapshot.getValue(Timesheet::class.java)
                        val timesheetDate = timesheet?.date?.toDate()
                        val startTime = timesheet?.startTime?.toTime()
                        val endTime = timesheet?.endTime?.toTime()

                        if (timesheetDate != null && startTime != null && endTime != null && endTime.after(startTime)) {
                            val hoursWorked = (endTime.time - startTime.time) / (1000.0 * 60 * 60)
                            dataPoints.add(hoursWorked.toFloat())
                        }
                    }
                }
                onResult(dataPoints)
            }
        }
    }

    private fun String.toDate(): Date? {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-d", Locale.getDefault()),   // Handles dates like "2024-11-3"
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),  // Handles dates like "2024-11-03"
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())   // Handles dates like "13/11/2024"
        )

        for (format in formats) {
            try {
                format.isLenient = false
                return format.parse(this)
            } catch (e: Exception) {
                Log.e("HoursActivity", "Date parsing error for format: ${format.toPattern()}, input: $this", e)
            }
        }

        Log.e("HoursActivity", "Unable to parse date: $this")
        return null
    }

    private fun String.toTime(): Date? {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            Log.e("HoursActivity", "Time parsing error for input: $this", e)
            null
        }
    }

    private fun Calendar.toFormattedString(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this.time)
    }

    private fun showGraphPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_graph, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val graphView = dialogView.findViewById<BarChartView>(R.id.graphView)
        val monthTextView = dialogView.findViewById<TextView>(R.id.monthTextView)
        val closeGraphButton = dialogView.findViewById<Button>(R.id.closeGraphButton)
        val selectDateButton = dialogView.findViewById<Button>(R.id.selectDateButton)

        selectDateButton.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                // Update the month title
                monthTextView.text = selectedDate.toFormattedString("MMMM")

                // Fetch the weekly data centered around the selected date
                fetchWeeklyData(selectedDate) { weeklyData, dateLabels ->
                    graphView.setData(weeklyData, dateLabels)
                }
            }
        }

        closeGraphButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun showGoalPerformancePopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_goal_performance, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val currentPeriodTextView = dialogView.findViewById<TextView>(R.id.currentPeriodTextView)
        val minGoalTextView = dialogView.findViewById<TextView>(R.id.minGoalTextView)
        val maxGoalTextView = dialogView.findViewById<TextView>(R.id.maxGoalTextView)
        val prevPeriodTextView = dialogView.findViewById<TextView>(R.id.prevPeriodTextView)
        val prevMinGoalTextView = dialogView.findViewById<TextView>(R.id.prevMinGoalTextView)
        val prevMaxGoalTextView = dialogView.findViewById<TextView>(R.id.prevMaxGoalTextView)
        val closeStatsButton = dialogView.findViewById<Button>(R.id.closeStatsButton)

        calculateGoalPerformance { currentPeriodStart, minGoalPercentage, maxGoalPercentage, prevPeriodStart, prevMinGoalPercentage, prevMaxGoalPercentage ->
            currentPeriodTextView.text = "Current Period Start: $currentPeriodStart"
            minGoalTextView.text = "Min Goal Met: ${"%.2f".format(minGoalPercentage)}%"
            maxGoalTextView.text = "Max Goal Met: ${"%.2f".format(maxGoalPercentage)}%"
            prevPeriodTextView.text = "Previous Period Start: $prevPeriodStart"
            prevMinGoalTextView.text = "Previous Min Goal: ${"%.2f".format(prevMinGoalPercentage)}%"
            prevMaxGoalTextView.text = "Previous Max Goal: ${"%.2f".format(prevMaxGoalPercentage)}%"
        }

        closeStatsButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun calculateGoalPerformance(onResult: (String, Float, Float, String, Float, Float) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time

        // Define the start date of the first 30-day period
        val firstPeriodStartDate = Calendar.getInstance().apply {
            set(2024, Calendar.NOVEMBER, 18)
        }

        // Check if the current date is before the start date
        if (currentDate.before(firstPeriodStartDate.time)) {
            onResult("No Data Available", 0f, 0f, "No Data Available", 0f, 0f)
            return
        }

        // Calculate the current 30-day period start date
        val daysSinceStart = ((currentDate.time - firstPeriodStartDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        val currentPeriodStartDate = firstPeriodStartDate.clone() as Calendar
        currentPeriodStartDate.add(Calendar.DAY_OF_MONTH, (daysSinceStart / 30) * 30)

        val previousPeriodStartDate = currentPeriodStartDate.clone() as Calendar
        previousPeriodStartDate.add(Calendar.DAY_OF_MONTH, -30)

        var daysMetMinGoal = 0
        var daysMetMaxGoal = 0
        var totalWeekdays = 0

        var prevDaysMetMinGoal = 0
        var prevDaysMetMaxGoal = 0
        var prevTotalWeekdays = 0

        val minGoal = minGoalEditText.text.toString().toFloatOrNull() ?: 0f
        val maxGoal = maxGoalEditText.text.toString().toFloatOrNull() ?: 0f

        val categoriesRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("categories")

        categoriesRef.get().addOnSuccessListener { categoriesSnapshot ->
            if (categoriesSnapshot.exists()) {
                categoriesSnapshot.children.forEach { categorySnapshot ->
                    val timesheetsRef = categorySnapshot.child("timesheets")

                    timesheetsRef.children.forEach { timesheetSnapshot ->
                        val timesheet = timesheetSnapshot.getValue(Timesheet::class.java)
                        val timesheetDate = timesheet?.date?.toDate()

                        if (timesheetDate != null) {
                            val startTime = timesheet?.startTime?.toTime()
                            val endTime = timesheet?.endTime?.toTime()

                            // Calculate hours worked
                            val hoursWorked = if (startTime != null && endTime != null && endTime.after(startTime)) {
                                (endTime.time - startTime.time) / (1000.0 * 60 * 60)
                            } else {
                                0.0
                            }

                            // Current period calculation
                            if (timesheetDate in currentPeriodStartDate.time..currentDate && isWeekday(timesheetDate)) {
                                totalWeekdays++
                                if (hoursWorked >= minGoal) daysMetMinGoal++
                                if (hoursWorked >= maxGoal) daysMetMaxGoal++
                            }

                            // Previous period calculation
                            if (timesheetDate >= previousPeriodStartDate.time && timesheetDate < currentPeriodStartDate.time && isWeekday(timesheetDate)) {
                                prevTotalWeekdays++
                                if (hoursWorked >= minGoal) prevDaysMetMinGoal++
                                if (hoursWorked >= maxGoal) prevDaysMetMaxGoal++
                            }
                        }
                    }
                }

                // Calculate percentages
                val minGoalPercentage = if (totalWeekdays > 0) (daysMetMinGoal.toFloat() / totalWeekdays) * 100 else 0f
                val maxGoalPercentage = if (totalWeekdays > 0) (daysMetMaxGoal.toFloat() / totalWeekdays) * 100 else 0f

                val prevMinGoalPercentage = if (prevTotalWeekdays > 0) (prevDaysMetMinGoal.toFloat() / prevTotalWeekdays) * 100 else 0f
                val prevMaxGoalPercentage = if (prevTotalWeekdays > 0) (prevDaysMetMaxGoal.toFloat() / prevTotalWeekdays) * 100 else 0f

                onResult(
                    currentPeriodStartDate.toFormattedString("dd/MM/yyyy"),
                    minGoalPercentage,
                    maxGoalPercentage,
                    previousPeriodStartDate.toFormattedString("dd/MM/yyyy"),
                    prevMinGoalPercentage,
                    prevMaxGoalPercentage
                )
            } else {
                // No data found
                onResult("No Data Available", 0f, 0f, "No Data Available", 0f, 0f)
            }
        }
    }
    private fun fetchWeeklyData(selectedDate: Calendar, onResult: (List<Float>, List<String>) -> Unit) {
        // Calculate the start and end dates for the 7-day range centered around the selected date
        val startOfRange = selectedDate.clone() as Calendar
        startOfRange.add(Calendar.DAY_OF_MONTH, -3)
        val endOfRange = selectedDate.clone() as Calendar
        endOfRange.add(Calendar.DAY_OF_MONTH, 3)

        val dateLabels = mutableListOf<String>()
        val weeklyData = MutableList(7) { 0f }

        // Generate date labels for the 7-day range
        val tempDate = startOfRange.clone() as Calendar
        for (i in 0 until 7) {
            dateLabels.add(tempDate.toFormattedString("dd/MM"))
            tempDate.add(Calendar.DAY_OF_MONTH, 1)
        }

        val categoriesRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("categories")

        categoriesRef.get().addOnSuccessListener { categoriesSnapshot ->
            if (categoriesSnapshot.exists()) {
                categoriesSnapshot.children.forEach { categorySnapshot ->
                    val timesheetsRef = categorySnapshot.child("timesheets")

                    timesheetsRef.children.forEach { timesheetSnapshot ->
                        val timesheet = timesheetSnapshot.getValue(Timesheet::class.java)
                        val timesheetDate = timesheet?.date?.toDate()
                        val startTime = timesheet?.startTime?.toTime()
                        val endTime = timesheet?.endTime?.toTime()

                        if (timesheetDate != null && startTime != null && endTime != null && endTime.after(startTime)) {
                            val hoursWorked = (endTime.time - startTime.time) / (1000.0 * 60 * 60)
                            if (timesheetDate in startOfRange.time..endOfRange.time) {
                                val dayIndex = ((timesheetDate.time - startOfRange.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                                if (dayIndex in 0..6) {
                                    weeklyData[dayIndex] += hoursWorked.toFloat()
                                }
                            }
                        }
                    }
                }
                onResult(weeklyData, dateLabels)
            }
        }
    }
    private fun Date.toCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = this
        return calendar
    }
    private fun Calendar.toFormattedString(pattern: String = "dd"): String {
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(this.time)
    }
    private fun isWeekday(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_WEEK) !in listOf(Calendar.SATURDAY, Calendar.SUNDAY)
    }
}