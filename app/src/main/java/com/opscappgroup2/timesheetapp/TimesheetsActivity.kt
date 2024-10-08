package com.opscappgroup2.timesheetapp
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TimesheetsActivity : AppCompatActivity() {

    private lateinit var categoryTextView: TextView
    private lateinit var timesheetsRecyclerView: RecyclerView
    private val timesheets = mutableListOf<Timesheet>()
    private lateinit var backToNavigationButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets)

        categoryTextView = findViewById(R.id.categoryTextView)
        timesheetsRecyclerView = findViewById(R.id.timesheetsRecyclerView)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userId = currentUser?.uid ?: "default_user"
        sharedPreferences = getSharedPreferences("UserTimesheets", Context.MODE_PRIVATE)

        backToNavigationButton.setOnClickListener {
            finish()
        }

        val categoryName = intent.getStringExtra("categoryName") ?: "No Category"
        categoryTextView.text = categoryName

        timesheetsRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TimesheetsAdapter(timesheets)
        timesheetsRecyclerView.adapter = adapter

        loadTimesheetsForCategory(categoryName, adapter)
    }

    private fun loadTimesheetsForCategory(categoryName: String, adapter: TimesheetsAdapter) {
        val jsonTimesheets = sharedPreferences.getString(userId + "_timesheets", null)
        if (!jsonTimesheets.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<Timesheet>>() {}.type
            val savedTimesheets: MutableList<Timesheet> = gson.fromJson(jsonTimesheets, type)

            val filteredTimesheets = savedTimesheets.filter { it.category == categoryName }

            timesheets.clear()
            timesheets.addAll(filteredTimesheets)
            adapter.notifyDataSetChanged()
        } else {
            categoryTextView.text = "No timesheets found for this category"
        }
    }
}