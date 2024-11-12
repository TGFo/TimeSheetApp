package com.opscappgroup2.timesheetapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoriesActivity : AppCompatActivity() {

    private val categories = mutableListOf<Category>()
    private lateinit var adapter: CategoriesAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        // Initialize Firebase Auth and get current user ID
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: return

        // Set up RecyclerView and Adapter
        val recyclerView: RecyclerView = findViewById(R.id.categoriesRecyclerView)
        adapter = CategoriesAdapter(this, categories, ::deleteCategory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load categories from Firebase
        loadCategories()

        // Handle back button click
        findViewById<Button>(R.id.backToNavigationButton).setOnClickListener {
            finish()
        }

        // Handle create category button click
        findViewById<View>(R.id.createCategoryButton).setOnClickListener {
            showCreateCategoryDialog()
        }

        // Handle open timesheet button click
        findViewById<Button>(R.id.openTimesheetButton).setOnClickListener {
            startActivity(Intent(this, TimesheetsCreateActivity::class.java))

        }
    }

    // Load categories from Firebase Realtime Database
    private fun loadCategories() {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userCategoriesRef = databaseRef.child("Users").child(userId).child("categories")

        userCategoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                if (snapshot.exists()) {
                    snapshot.children.forEach { child ->
                        val category = child.getValue(Category::class.java)
                        category?.let {
                            it.id = child.key // Set Firebase key as the category ID
                            categories.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@CategoriesActivity, "No categories found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CategoriesActivity, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Save a new category to Firebase
    private fun saveCategory(category: Category) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userCategoriesRef = databaseRef.child("Users").child(userId).child("categories")

        // Generate a unique key for the new category
        val categoryKey = userCategoriesRef.push().key ?: return
        category.id = categoryKey // Store the generated key in the category object

        userCategoriesRef.child(categoryKey).setValue(category)
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete a category and remove it from Firebase
    private fun deleteCategory(category: Category) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userCategoriesRef = databaseRef.child("Users").child(userId).child("categories")

        // Remove the category from the local list
        categories.remove(category)
        adapter.notifyDataSetChanged()

        // Delete the category from Firebase using its unique ID
        category.id?.let { categoryId ->
            userCategoriesRef.child(categoryId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Category deleted.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to delete category: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Error: Category ID is missing.", Toast.LENGTH_SHORT).show()
        }
    }

    // Show dialog to create a new category
    private fun showCreateCategoryDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_category, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.categoryNameEditText)
        val descEditText = dialogView.findViewById<EditText>(R.id.categoryDescEditText)

        AlertDialog.Builder(this)
            .setTitle("Create New Category")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val categoryName = nameEditText.text.toString().trim()
                val categoryDescription = descEditText.text.toString().trim()

                if (categoryName.isNotEmpty() && categoryDescription.isNotEmpty()) {
                    val newCategory = Category(name = categoryName, description = categoryDescription)
                    categories.add(newCategory)
                    saveCategory(newCategory) // Save the new category to Firebase
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}