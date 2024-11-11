package com.opscappgroup2.timesheetapp

import android.app.DatePickerDialog
import android.os.Bundle
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

        // Load goals from Firebase
        loadGoals()

        saveGoalsButton.setOnClickListener { saveGoals() }
        backToNavigationButton.setOnClickListener { finish() }

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

    // Load goals from Firebase Realtime Database
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

    // Save goals to Firebase Realtime Database
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

    // Show Date Picker dialog
    private fun showDatePickerDialog(onDateSet: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, day)
            onDateSet(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Display hours report based on selected date range
    private fun displayHoursReport(startDate: Calendar, endDate: Calendar) {
        val timesheetsRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("timesheets")

        timesheetsRef.get().addOnSuccessListener { snapshot ->
            var totalHoursWorked = 0.0
            if (snapshot.exists()) {
                snapshot.children.forEach { child ->
                    val timesheet = child.getValue(Timesheet::class.java)
                    val timesheetDate = timesheet?.date?.toDate()
                    if (timesheetDate != null && timesheetDate in startDate.time..endDate.time) {
                        val startTime = timesheet.startTime.toTime()
                        val endTime = timesheet.endTime.toTime()
                        if (startTime != null && endTime != null) {
                            totalHoursWorked += (endTime.time - startTime.time) / (1000.0 * 60 * 60)
                        }
                    }
                }
                totalHoursTextView.text = "Total hours worked: %.2f hours".format(totalHoursWorked)
            } else {
                totalHoursTextView.text = "No timesheets found."
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to generate report: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to convert String to Date
    private fun String.toDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    // Helper function to convert String to Time
    private fun String.toTime(): Date? {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            null
        }
    }
}