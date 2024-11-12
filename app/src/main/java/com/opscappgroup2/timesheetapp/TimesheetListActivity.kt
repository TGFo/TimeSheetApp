package com.opscappgroup2.timesheetapp

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
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
                startDateTextView.text = date
                filterTimesheetsByDate()
            }
        }

        selectEndDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                endDateTextView.text = date
                filterTimesheetsByDate()
            }
        }

        loadTimesheets()
    }

    // Show DatePickerDialog for date selection
    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Load all timesheets from Firebase Realtime Database
    private fun loadTimesheets() {
        val userTimesheetsRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(userId).child("categories").child("timesheets")

        userTimesheetsRef.get().addOnSuccessListener { snapshot ->
            timesheets.clear()
            if (snapshot.exists()) {
                snapshot.children.mapNotNullTo(timesheets) { it.getValue(Timesheet::class.java) }
                adapter.updateTimesheets(timesheets)
                Toast.makeText(this, "Loaded ${timesheets.size} timesheets", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No timesheets found in Firebase.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load timesheets: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Filter timesheets based on selected start and end dates
    private fun filterTimesheetsByDate() {
        val startDateString = startDateTextView.text.toString()
        val endDateString = endDateTextView.text.toString()

        if (startDateString.isBlank() || endDateString.isBlank() ||
            startDateString == "Select Start Date" || endDateString == "Select End Date"
        ) {
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

    // Convert a String to a Date object
    private fun String.toDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            Log.e("TimesheetListActivity", "Date parsing error: $this", e)
            null
        }
    }

    // Open the photo associated with the timesheet
    private fun openPhoto(timesheet: Timesheet) {
        timesheet.photoUri?.let {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(it), "image/*")
            }
            startActivity(intent)
        } ?: Toast.makeText(this, "No photo available for this timesheet", Toast.LENGTH_SHORT).show()
    }
}