package com.opscappgroup2.timesheetapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NavigationActivity : AppCompatActivity() {

    private lateinit var buttonCategories: Button
    private lateinit var buttonHours: Button
    private lateinit var buttonTimesheetList: Button
    private lateinit var buttonProfilePage: Button
    private lateinit var buttonGacha: Button
    private lateinit var buttonChuddieList: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        buttonCategories = findViewById(R.id.buttonCategories)
        buttonHours = findViewById(R.id.buttonHours)
        buttonTimesheetList = findViewById(R.id.buttonTimesheetList)
        buttonProfilePage = findViewById(R.id.buttonProfilePage)
        buttonGacha = findViewById(R.id.buttonGacha)
        buttonChuddieList = findViewById(R.id.buttonChuddieList)

        buttonCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }

        buttonHours.setOnClickListener {
            startActivity(Intent(this, HoursActivity::class.java))
        }

        buttonTimesheetList.setOnClickListener {
            startActivity(Intent(this, TimesheetListActivity::class.java))
        }

        buttonProfilePage.setOnClickListener {
            startActivity(Intent(this, ProfilePageActivity::class.java))
        }

        buttonGacha.setOnClickListener {
            startActivity(Intent(this, GachaActivity::class.java))
        }

        buttonChuddieList.setOnClickListener {
            startActivity(Intent(this, ChuddieListActivity::class.java))
        }
    }
}