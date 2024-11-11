package com.opscappgroup2.timesheetapp

import android.os.Bundle
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
        val categoryName = intent.getStringExtra("categoryName") ?: "No Category"
        categoryTextView.text = categoryName

        timesheetsRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TimesheetsAdapter(timesheets)
        timesheetsRecyclerView.adapter = adapter

        loadTimesheetsForCategory(categoryName, adapter)
    }

    // Load timesheets for the selected category from Firebase
    private fun loadTimesheetsForCategory(categoryName: String, adapter: TimesheetsAdapter) {
        val userTimesheetsRef = FirebaseDatabase.getInstance().reference
            .child("Users").child(userId).child("timesheets")

        userTimesheetsRef.get().addOnSuccessListener { snapshot ->
            timesheets.clear()
            if (snapshot.exists()) {
                snapshot.children.mapNotNull { it.getValue(Timesheet::class.java) }
                    .filter { it.category == categoryName }
                    .also { timesheets.addAll(it) }

                adapter.notifyDataSetChanged()

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