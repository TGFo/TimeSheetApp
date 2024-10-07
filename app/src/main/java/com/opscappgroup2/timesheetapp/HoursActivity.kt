package com.opscappgroup2.timesheetapp

import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HoursActivity : AppCompatActivity() {

    private lateinit var minGoalEditText: EditText
    private lateinit var maxGoalEditText: EditText
    private lateinit var saveGoalsButton: Button
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var viewReportButton: Button
    private lateinit var backToNavigationButton: Button

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hours)

        // Initialize views
        minGoalEditText = findViewById(R.id.minGoalEditText)
        maxGoalEditText = findViewById(R.id.maxGoalEditText)
        saveGoalsButton = findViewById(R.id.saveGoalsButton)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        viewReportButton = findViewById(R.id.viewReportButton)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)


        // Load any previously saved goals
        val sharedPrefs = getSharedPreferences("HourGoals", Context.MODE_PRIVATE)
        val savedMinGoal = sharedPrefs.getFloat("minGoal", 0f)
        val savedMaxGoal = sharedPrefs.getFloat("maxGoal", 0f)

        if (savedMinGoal > 0) {
            minGoalEditText.setText(savedMinGoal.toString())
        }
        if (savedMaxGoal > 0) {
            maxGoalEditText.setText(savedMaxGoal.toString())
        }

        // Save goals when button is clicked
        saveGoalsButton.setOnClickListener {
            val minGoal = minGoalEditText.text.toString().toFloatOrNull()
            val maxGoal = maxGoalEditText.text.toString().toFloatOrNull()

            if (minGoal == null || maxGoal == null) {
                Toast.makeText(this, "Please enter valid goals", Toast.LENGTH_SHORT).show()
            } else if (minGoal > maxGoal) {
                Toast.makeText(this, "Min goal cannot be greater than max goal", Toast.LENGTH_SHORT).show()
            } else {
                val editor = sharedPrefs.edit()
                editor.putFloat("minGoal", minGoal)
                editor.putFloat("maxGoal", maxGoal)
                editor.apply()

                Toast.makeText(this, "Goals saved", Toast.LENGTH_SHORT).show()
            }
        }
        backToNavigationButton.setOnClickListener {

            finish()
        }

        // Handle date picker for start date
        startDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                startDate = date
                startDateButton.text = "${date.get(Calendar.DAY_OF_MONTH)}/${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.YEAR)}"
            }
        }

        // Handle date picker for end date
        endDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                endDate = date
                endDateButton.text = "${date.get(Calendar.DAY_OF_MONTH)}/${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.YEAR)}"
            }
        }
        viewReportButton.setOnClickListener {
            if (startDate != null && endDate != null) {
                displayHoursReport(startDate!!, endDate!!)
            } else {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showDatePickerDialog(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                onDateSet(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }
    private fun displayHoursReport(startDate: Calendar, endDate: Calendar) {
        // Logic to calculate total hours for each category based on the selected period
        // This could involve querying a database or in-memory list

        // For now, show a placeholder Toast
        Toast.makeText(this, "Displaying hours report for selected period", Toast.LENGTH_SHORT).show()

        // You can replace this with actual logic to display the report in the UI
    }
}