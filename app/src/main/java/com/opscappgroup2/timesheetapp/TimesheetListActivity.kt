package com.opscappgroup2.timesheetapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class TimesheetListActivity : AppCompatActivity() {

    private lateinit var selectStartDateButton: Button
    private lateinit var selectEndDateButton: Button
    private lateinit var startDateTextView: TextView
    private lateinit var endDateTextView: TextView
    private lateinit var timesheetRecyclerView: RecyclerView
    private lateinit var backToNavigationButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private val timesheets = mutableListOf<Timesheet>()
    private lateinit var adapter: TimesheetsAdapter
    private var startDate: Calendar? = null
    private var endDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_list)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: return

        selectStartDateButton = findViewById(R.id.selectStartDateButton)
        selectEndDateButton = findViewById(R.id.selectEndDateButton)
        startDateTextView = findViewById(R.id.startDateTextView)
        endDateTextView = findViewById(R.id.endDateTextView)
        timesheetRecyclerView = findViewById(R.id.timesheetRecyclerView)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)

        adapter = TimesheetsAdapter(timesheets)
        timesheetRecyclerView.layoutManager = LinearLayoutManager(this)
        timesheetRecyclerView.adapter = adapter

        backToNavigationButton.setOnClickListener { finish() }

        selectStartDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                startDate = date
                startDateTextView.text = date.toFormattedString()
                filterTimesheetsByDate()
            }
        }

        selectEndDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                endDate = date
                endDateTextView.text = date.toFormattedString()
                filterTimesheetsByDate()
            }
        }

        // Load all timesheets initially
        loadAllTimesheets()
    }

    private fun showDatePickerDialog(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            onDateSelected(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Load all timesheets from all categories
    private fun loadAllTimesheets() {
        val categoriesRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("categories")

        categoriesRef.get().addOnSuccessListener { categoriesSnapshot ->
            timesheets.clear()
            if (categoriesSnapshot.exists()) {
                categoriesSnapshot.children.forEach { categorySnapshot ->
                    val timesheetsRef = categorySnapshot.child("timesheets")

                    timesheetsRef.children.forEach { timesheetSnapshot ->
                        val timesheet = timesheetSnapshot.getValue(Timesheet::class.java)
                        if (timesheet != null) {
                            timesheets.add(timesheet)
                        }
                    }
                }
                adapter.updateTimesheets(timesheets)
                Toast.makeText(this, "Loaded ${timesheets.size} timesheets", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No timesheets found.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load timesheets: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("TimesheetListActivity", "Error loading timesheets: ${e.message}", e)
        }
    }

    // Filter timesheets based on selected start and end dates
    private fun filterTimesheetsByDate() {
        val startDateString = startDateTextView.text.toString()
        val endDateString = endDateTextView.text.toString()

        if (startDateString.isBlank() || endDateString.isBlank()) {
            Toast.makeText(this, "Please select both start and end dates.", Toast.LENGTH_SHORT).show()
            return
        }

        val startDate = startDateString.toDate()
        val endDate = endDateString.toDate()

        if (startDate == null || endDate == null) {
            Toast.makeText(this, "Invalid date format. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        if (startDate.after(endDate)) {
            Toast.makeText(this, "Start date cannot be after end date.", Toast.LENGTH_SHORT).show()
            return
        }

        val filteredTimesheets = timesheets.filter {
            val timesheetDate = it.date.toDate()
            timesheetDate != null && (timesheetDate in startDate..endDate)
        }

        adapter.updateTimesheets(filteredTimesheets)

        if (filteredTimesheets.isEmpty()) {
            Toast.makeText(this, "No timesheets found for the selected date range.", Toast.LENGTH_SHORT).show()
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

    private fun Calendar.toFormattedString(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this.time)
    }
}