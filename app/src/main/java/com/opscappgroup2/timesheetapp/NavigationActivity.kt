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

        buttonCategories = findViewById(R.id.buttonCategories)
        buttonHours = findViewById(R.id.buttonHours)
        buttonTimesheetList = findViewById(R.id.buttonTimesheetList)

        buttonCategories.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        buttonHours.setOnClickListener {
            val intent = Intent(this, HoursActivity::class.java)
            startActivity(intent)
        }

        buttonTimesheetList.setOnClickListener {
            val intent = Intent(this, TimesheetListActivity::class.java)
            startActivity(intent)
        }
    }
}