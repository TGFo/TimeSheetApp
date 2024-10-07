package com.opscappgroup2.timesheetapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoriesActivity : AppCompatActivity() {

    private val categories = mutableListOf<Category>()
    private lateinit var adapter: CategoriesAdapter
    private lateinit var backToNavigationButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        auth = FirebaseAuth.getInstance()

        // Get the current logged-in user's UID (this will be the unique key to save their data)
        val currentUser = auth.currentUser
        userId = currentUser?.uid ?: "default_user"

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserCategories", Context.MODE_PRIVATE)

        // Set up RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.categoriesRecyclerView)
        adapter = CategoriesAdapter(this, categories, ::deleteCategory)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load categories for the current user
        loadCategories()

        // Back to navigation button
        backToNavigationButton = findViewById(R.id.backToNavigationButton)
        backToNavigationButton.setOnClickListener {
            finish()
        }

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
        saveCategories() // Save the updated categories
        adapter.notifyDataSetChanged() // Notify adapter that data set has changed
    }

    // Save categories to SharedPreferences for the current user
    private fun saveCategories() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val jsonCategories = gson.toJson(categories)
        editor.putString(userId, jsonCategories)
        editor.apply()
    }

    // Load categories for the current user from SharedPreferences
    private fun loadCategories() {
        val jsonCategories = sharedPreferences.getString(userId, null)
        if (!jsonCategories.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<Category>>() {}.type
            val savedCategories: MutableList<Category> = gson.fromJson(jsonCategories, type)
            categories.clear()
            categories.addAll(savedCategories)
            adapter.notifyDataSetChanged()
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
                    saveCategories() // Save the categories after adding a new one
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