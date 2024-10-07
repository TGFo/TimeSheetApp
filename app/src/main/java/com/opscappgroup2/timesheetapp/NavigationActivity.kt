package com.opscappgroup2.timesheetapp
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NavigationActivity : AppCompatActivity() {

    private lateinit var buttonCategories: Button
    private lateinit var buttonHours: Button
    private lateinit var buttonTimesheetList: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        // Initialize buttons
        buttonCategories = findViewById(R.id.buttonCategories)
        buttonHours = findViewById(R.id.buttonHours)
        buttonTimesheetList = findViewById(R.id.buttonTimesheetList)

        // Set up click listeners for navigation
        buttonCategories.setOnClickListener {
            // Navigate to CategoriesActivity
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        buttonHours.setOnClickListener {
            // Navigate to HoursActivity
            val intent = Intent(this, HoursActivity::class.java)
            startActivity(intent)
        }

        buttonTimesheetList.setOnClickListener {
            // Navigate to TimesheetListActivity
            val intent = Intent(this, TimesheetListActivity::class.java)
            startActivity(intent)
        }
    }
}