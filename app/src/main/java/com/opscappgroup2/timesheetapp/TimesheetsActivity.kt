package com.opscappgroup2.timesheetapp
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TimesheetsActivity : AppCompatActivity() {
    private lateinit var categoryTextView: TextView
    private lateinit var timesheetsRecyclerView: RecyclerView
    private val timesheets = mutableListOf<Timesheet>() // Timesheets list
    private lateinit var backToNavigationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets)


        categoryTextView = findViewById(R.id.categoryTextView)
        timesheetsRecyclerView = findViewById(R.id.timesheetsRecyclerView)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)

        backToNavigationButton.setOnClickListener {

            finish()
        }
        // Get the category name from the Intent
        val categoryName = intent.getStringExtra("categoryName") ?: "No Category"

        // Set the category name to the TextView
        categoryTextView.text = categoryName

        // Set up RecyclerView
        timesheetsRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TimesheetsAdapter(timesheets) // Adapter for displaying timesheets
        timesheetsRecyclerView.adapter = adapter

        // Load timesheets for the selected category (this would be from your data source)
        loadTimesheetsForCategory(categoryName, adapter)
    }

    private fun loadTimesheetsForCategory(categoryName: String, adapter: TimesheetsAdapter) {
        // TODO: Load timesheets from your data source (e.g., database, API, etc.) based on the category
        // For demonstration purposes, let's add some mock data

        val mockTimesheets = listOf(
            Timesheet("2024-10-01", "09:00 AM", "05:00 PM", "Worked on project A", null,categoryName),
            Timesheet("2024-10-02", "10:00 AM", "06:00 PM", "Finished task B", null, categoryName)
        )

        timesheets.addAll(mockTimesheets.filter { it.category == categoryName })
        adapter.notifyDataSetChanged() // Notify adapter that data has changed
    }
}