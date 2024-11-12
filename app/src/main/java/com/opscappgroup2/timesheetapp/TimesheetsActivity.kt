package com.opscappgroup2.timesheetapp

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

class TimesheetsActivity : AppCompatActivity() {

    private lateinit var categoryTextView: TextView
    private lateinit var timesheetsRecyclerView: RecyclerView
    private lateinit var backToNavigationButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private val timesheets = mutableListOf<Timesheet>()
    private lateinit var adapter: TimesheetsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: return

        categoryTextView = findViewById(R.id.categoryTextView)
        timesheetsRecyclerView = findViewById(R.id.timesheetsRecyclerView)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)

        backToNavigationButton.setOnClickListener { finish() }

        // Get category name from the intent
        val categoryId = intent.getStringExtra("categoryId")
        val categoryName = intent.getStringExtra("categoryName")

        if (categoryName != null) {
            categoryTextView.text = categoryName
        } else {
            categoryTextView.text = "No Category Selected"
            Toast.makeText(this, "Error: Category name not received", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("TimesheetsActivity", "Received categoryId: $categoryId, categoryName: $categoryName")

        adapter = TimesheetsAdapter(timesheets)
        timesheetsRecyclerView.layoutManager = LinearLayoutManager(this)
        timesheetsRecyclerView.adapter = adapter

        if (categoryId != null) {
            loadTimesheetsForCategory(categoryId)
        } else {
            Toast.makeText(this, "Error: Category ID not received", Toast.LENGTH_SHORT).show()
        }
    }


    // Load timesheets for the selected category from Firebase
    // Load timesheets for the selected category using categoryId
    private fun loadTimesheetsForCategory(categoryId: String) {
        val categoryTimesheetsRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(userId).child("categories").child(categoryId).child("timesheets")

        categoryTimesheetsRef.get().addOnSuccessListener { snapshot ->
            timesheets.clear()
            if (snapshot.exists()) {
                // Map the snapshot data to Timesheet objects and add them to the list
                snapshot.children.mapNotNull { it.getValue(Timesheet::class.java) }
                    .also { timesheets.addAll(it) }

                // Notify the adapter about data changes
                adapter.updateTimesheets(timesheets)

                if (timesheets.isEmpty()) {
                    categoryTextView.text = "No timesheets found for this category"
                }
            } else {
                categoryTextView.text = "No timesheets found for this category"
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load timesheets: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}