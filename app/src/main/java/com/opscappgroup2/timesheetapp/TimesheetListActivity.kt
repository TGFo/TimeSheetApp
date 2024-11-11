package com.opscappgroup2.timesheetapp

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_list)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: return

        selectStartDateButton = findViewById(R.id.selectStartDateButton)
        selectEndDateButton = findViewById(R.id.selectEndDateButton)
        startDateTextView = findViewById(R.id.startDateTextView)
        endDateTextView = findViewById(R.id.endDateTextView)
        timesheetRecyclerView = findViewById(R.id.timesheetRecyclerView)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)

        timesheetRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TimesheetsAdapter(timesheets)
        timesheetRecyclerView.adapter = adapter

        backToNavigationButton.setOnClickListener { finish() }

        selectStartDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                startDateTextView.text = date
                filterTimesheetsByDate(adapter)
            }
        }

        selectEndDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                endDateTextView.text = date
                filterTimesheetsByDate(adapter)
            }
        }

        loadTimesheets(adapter)
    }

    // Display DatePickerDialog for date selection
    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val date = "$year-${month + 1}-$day"
            onDateSelected(date)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Filter timesheets based on selected start and end dates
    private fun filterTimesheetsByDate(adapter: TimesheetsAdapter) {
        val startDateString = startDateTextView.text.toString()
        val endDateString = endDateTextView.text.toString()

        if (startDateString != "Select Start Date" && endDateString != "Select End Date") {
            val startDate = startDateString.toDate()
            val endDate = endDateString.toDate()

            if (startDate != null && endDate != null) {
                val filteredTimesheets = timesheets.filter {
                    val timesheetDate = it.date.toDate()
                    timesheetDate != null && (timesheetDate in startDate..endDate)
                }
                adapter.updateTimesheets(filteredTimesheets)
            }
        }
    }

    // Load timesheets from Firebase Realtime Database
    private fun loadTimesheets(adapter: TimesheetsAdapter) {
        val userTimesheetsRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(userId).child("timesheets")

        userTimesheetsRef.get().addOnSuccessListener { snapshot ->
            timesheets.clear()
            if (snapshot.exists()) {
                snapshot.children.mapNotNullTo(timesheets) { it.getValue(Timesheet::class.java) }
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "No timesheets found in Firebase.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load timesheets: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to convert a String to a Date object
    fun String.toDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
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