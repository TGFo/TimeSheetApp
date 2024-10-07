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
import java.text.SimpleDateFormat
import java.util.*

class TimesheetListActivity : AppCompatActivity() {

    private lateinit var selectStartDateButton: Button
    private lateinit var selectEndDateButton: Button
    private lateinit var startDateTextView: TextView
    private lateinit var endDateTextView: TextView
    private lateinit var timesheetRecyclerView: RecyclerView
    private lateinit var backToNavigationButton: Button
    private val timesheets = mutableListOf<Timesheet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_list)

        selectStartDateButton = findViewById(R.id.selectStartDateButton)
        selectEndDateButton = findViewById(R.id.selectEndDateButton)
        startDateTextView = findViewById(R.id.startDateTextView)
        endDateTextView = findViewById(R.id.endDateTextView)
        timesheetRecyclerView = findViewById(R.id.timesheetRecyclerView)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)

        // Set up RecyclerView
        timesheetRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TimesheetsAdapter(timesheets)
        timesheetRecyclerView.adapter = adapter

        backToNavigationButton.setOnClickListener {

            finish()
        }
        // Select start date
        selectStartDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                startDateTextView.text = date
                filterTimesheetsByDate(adapter)
            }
        }

        // Select end date
        selectEndDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                endDateTextView.text = date
                filterTimesheetsByDate(adapter)
            }
        }

        // Load the initial timesheets (without date filtering)
        loadTimesheets(adapter)
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            onDateSelected(date)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun filterTimesheetsByDate(adapter: TimesheetsAdapter) {
        val startDateString = startDateTextView.text.toString()
        val endDateString = endDateTextView.text.toString()

        if (startDateString != "Select Start Date" && endDateString != "Select End Date") {
            val startDate = startDateString.toDate()
            val endDate = endDateString.toDate()

            if (startDate != null && endDate != null) {
                val filteredTimesheets = timesheets.filter {
                    val timesheetDate = it.date.toDate() // Convert timesheet date to Date object
                    timesheetDate != null && (timesheetDate == startDate || timesheetDate == endDate ||
                            (timesheetDate.after(startDate) && timesheetDate.before(endDate)))
                }

                adapter.updateTimesheets(filteredTimesheets)
            }
        }
    }

    private fun loadTimesheets(adapter: TimesheetsAdapter) {
        // Load timesheets from your data source (database or API)
        val mockTimesheets = listOf(
            Timesheet("2024-10-01", "09:00 AM", "05:00 PM", "Worked on project A", "photo_uri_1", "Category 1"),
            Timesheet("2024-10-03", "10:00 AM", "06:00 PM", "Completed task B", "photo_uri_2", "Category 1"),
            Timesheet("2024-10-05", "11:00 AM", "04:00 PM", "Reviewed task C", null, "Category 2")
        )
        timesheets.addAll(mockTimesheets)
        adapter.notifyDataSetChanged()
    }

    fun String.toDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            null // If there's an error, return null
        }
    }

    private fun openPhoto(timesheet: Timesheet) {
        if (timesheet.photoUri != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(timesheet.photoUri), "image/*")
            startActivity(intent)
        } else {
            Toast.makeText(this, "No photo available for this timesheet", Toast.LENGTH_SHORT).show()
        }
    }
}