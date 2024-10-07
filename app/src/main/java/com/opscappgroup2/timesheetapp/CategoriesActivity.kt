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
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoriesActivity : AppCompatActivity() {

    private val categories = mutableListOf<Category>()
    private lateinit var adapter: CategoriesAdapter
    private lateinit var backToNavigationButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        sharedPreferences = getSharedPreferences("CategoriesPrefs", MODE_PRIVATE)

        // Load categories from SharedPreferences
        loadCategories()

        // Set up RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.categoriesRecyclerView)
        adapter = CategoriesAdapter(this, categories, ::deleteCategory)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        backToNavigationButton = findViewById(R.id.backToNavigationButton)
        backToNavigationButton.setOnClickListener {
            saveCategories() // Save categories before navigating away
            finish()
        }

        // Set up the "Create Category" button
        val createCategoryButton: View = findViewById(R.id.createCategoryButton)
        createCategoryButton.setOnClickListener {
            showCreateCategoryDialog()
        }
    }

    // Save categories to SharedPreferences as JSON
    private fun saveCategories() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(categories)
        editor.putString("categories_list", json)
        editor.apply()
    }

    // Load categories from SharedPreferences
    private fun loadCategories() {
        val gson = Gson()
        val json = sharedPreferences.getString("categories_list", null)
        if (json != null) {
            val type = object : TypeToken<List<Category>>() {}.type
            val savedCategories: List<Category> = gson.fromJson(json, type)
            categories.addAll(savedCategories)
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
                    saveCategories() // Save the updated list
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun deleteCategory(category: Category) {
        categories.remove(category)
        adapter.notifyDataSetChanged()
        saveCategories() // Save the updated list after deletion
    }
}