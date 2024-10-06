package com.opscappgroup2.timesheetapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CategoriesActivity : AppCompatActivity() {
    private val categories = mutableListOf<Category>()
    private lateinit var adapter: CategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        // Set up RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.categoriesRecyclerView)
        adapter = CategoriesAdapter(this, categories, ::deleteCategory)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Populate with some initial categories
        setUpCategories()

        // Set up the "Create Category" button
        val createCategoryButton: View = findViewById(R.id.createCategoryButton)
        createCategoryButton.setOnClickListener {
            showCreateCategoryDialog()
        }
        // Handle button to navigate to TimesheetsCreateActivity
        val openTimesheetButton: Button = findViewById(R.id.openTimesheetButton)
        openTimesheetButton.setOnClickListener {
            val intent = Intent(this, TimesheetsCreateActivity::class.java)
            startActivity(intent)
        }
    }

    private fun deleteCategory(category: Category) {
        categories.remove(category) // Remove category from list
        adapter.notifyDataSetChanged() // Notify adapter that data set has changed
    }
    // Method to set up initial categories
    private fun setUpCategories() {
        val testNames = listOf("1", "2", "3", "4", "5")
        val testDescriptions = listOf("desc1", "desc2", "desc3", "desc4", "desc5")
        for (i in testNames.indices) {
            categories.add(Category(testNames[i], testDescriptions[i]))
        }
    }

    // Method to show the dialog for creating a new category
    private fun showCreateCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_category, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.categoryNameEditText)
        val descEditText = dialogView.findViewById<EditText>(R.id.categoryDescEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Create New Category")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val categoryName = nameEditText.text.toString()
                val categoryDescription = descEditText.text.toString()

                if (!TextUtils.isEmpty(categoryName) && !TextUtils.isEmpty(categoryDescription)) {
                    // Add new category to the list
                    categories.add(Category(categoryName, categoryDescription))
                    adapter.notifyDataSetChanged() // Notify adapter to refresh the list
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
}