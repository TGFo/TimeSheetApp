package com.opscappgroup2.timesheetapp


import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hours)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userId = currentUser?.uid ?: "default_user"

        sharedPreferences = getSharedPreferences("UserTimesheets", Context.MODE_PRIVATE)

        minGoalEditText = findViewById(R.id.minGoalEditText)
        maxGoalEditText = findViewById(R.id.maxGoalEditText)
        saveGoalsButton = findViewById(R.id.saveGoalsButton)
        startDateButton = findViewById(R.id.startDateButton)
        endDateButton = findViewById(R.id.endDateButton)
        viewReportButton = findViewById(R.id.viewReportButton)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)
        totalHoursTextView = findViewById(R.id.totalHoursTextView)

        val sharedPrefs = getSharedPreferences("HourGoals", Context.MODE_PRIVATE)
        val savedMinGoal = sharedPrefs.getFloat("minGoal", 0f)
        val savedMaxGoal = sharedPrefs.getFloat("maxGoal", 0f)

        if (savedMinGoal > 0) {
            minGoalEditText.setText(savedMinGoal.toString())
        }
        if (savedMaxGoal > 0) {
            maxGoalEditText.setText(savedMaxGoal.toString())
        }

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

        startDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                startDate = date
                startDateButton.text = "${date.get(Calendar.DAY_OF_MONTH)}/${date.get(Calendar.MONTH) + 1}/${date.get(Calendar.YEAR)}"
            }
        }

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
        val jsonTimesheets = sharedPreferences.getString(userId + "_timesheets", null)
        if (jsonTimesheets.isNullOrEmpty()) {
            totalHoursTextView.text = "No timesheets found."
            return
        }

        val gson = Gson()
        val type = object : TypeToken<MutableList<Timesheet>>() {}.type
        val savedTimesheets: MutableList<Timesheet> = gson.fromJson(jsonTimesheets, type)

        var totalHoursWorked = 0.0

        val filteredTimesheets = savedTimesheets.filter {
            val timesheetDate = it.date.toDate()
            timesheetDate != null && (timesheetDate == startDate.time || timesheetDate == endDate.time ||
                    (timesheetDate.after(startDate.time) && timesheetDate.before(endDate.time)))
        }

        for (timesheet in filteredTimesheets) {
            val startTime = timesheet.startTime.toTime()
            val endTime = timesheet.endTime.toTime()

            if (startTime != null && endTime != null) {
                val hoursWorked = (endTime.time - startTime.time) / (1000.0 * 60 * 60)
                totalHoursWorked += hoursWorked
            }
        }

        totalHoursTextView.text = "Total hours worked: %.2f hours".format(totalHoursWorked)
    }

    fun String.toDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    fun String.toTime(): Date? {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            null
        }
    }
}