package com.opscappgroup2.timesheetapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class TimesheetsCreateActivity : AppCompatActivity() {

    private lateinit var photoImageView: ImageView
    private var selectedImageUri: Uri? = null
    private var selectedCategoryId: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private val timesheets = mutableListOf<Timesheet>()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var categorySpinner: Spinner
    private val categoryMap = mutableMapOf<String, String>() // Map to store category names and IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_create)

        // Initialize FirebaseAuth and get user ID
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: "default_user"

        photoImageView = findViewById(R.id.photoImageView)
        val dateEditText: EditText = findViewById(R.id.dateEditText)
        val startTimeEditText: EditText = findViewById(R.id.startTimeEditText)
        val endTimeEditText: EditText = findViewById(R.id.endTimeEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        val saveButton: Button = findViewById(R.id.saveButton)

        // Set up image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data
                photoImageView.setImageURI(selectedImageUri)
                photoImageView.visibility = View.VISIBLE
            }
        }

        // Load categories into the spinner
        loadCategoriesIntoSpinner()

        // Handle photo selection
        addPhotoButton.setOnClickListener {
            openGalleryForImage()
        }

        // Set date picker for the date field
        dateEditText.setOnClickListener {
            showDatePickerDialog { date -> dateEditText.setText(date) }
        }

        // Set time picker for the start time
        startTimeEditText.setOnClickListener {
            showTimePickerDialog { time -> startTimeEditText.setText(time) }
        }

        // Set time picker for the end time
        endTimeEditText.setOnClickListener {
            showTimePickerDialog { time -> endTimeEditText.setText(time) }
        }

        // Handle saving the timesheet entry
        saveButton.setOnClickListener {
            val date = dateEditText.text.toString()
            val startTime = startTimeEditText.text.toString()
            val endTime = endTimeEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val selectedCategoryName = categorySpinner.selectedItem?.toString()
            val categoryId = categoryMap[selectedCategoryName] // Get the category ID from the map

            if (!validateFields(date, startTime, endTime, description) || categoryId == null) {
                Toast.makeText(this, "Please fill all the fields and select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save timesheet entry with the selected category ID
            saveTimesheetEntry(
                Timesheet(date, startTime, endTime, description, selectedImageUri?.toString(), categoryId)
            )
        }
    }

    // Load categories from Firebase and populate the spinner
    private fun loadCategoriesIntoSpinner() {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userCategoriesRef = databaseRef.child("Users").child(userId).child("categories")

        userCategoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryNames = mutableListOf<String>()
                categoryMap.clear() // Clear any existing data

                for (child in snapshot.children) {
                    val category = child.getValue(Category::class.java)
                    category?.let {
                        val categoryId = child.key
                        val categoryName = it.name
                        if (!categoryName.isNullOrEmpty() && !categoryId.isNullOrEmpty()) {
                            categoryNames.add(categoryName)
                            categoryMap[categoryName] = categoryId // Map name to ID
                        }
                    }
                }

                if (categoryNames.isEmpty()) {
                    Toast.makeText(this@TimesheetsCreateActivity, "No categories found", Toast.LENGTH_SHORT).show()
                }

                // Populate the spinner with category names
                val adapter = ArrayAdapter(this@TimesheetsCreateActivity, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TimesheetsCreateActivity, "Failed to load categories: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Open the gallery to select an image
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    // Show Date Picker dialog
    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val date = "$year-${month + 1}-$dayOfMonth"
            onDateSelected(date)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // Show Time Picker dialog
    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(this, { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            onTimeSelected(formattedTime)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    // Validate input fields
    private fun validateFields(date: String, startTime: String, endTime: String, description: String): Boolean {
        return !(TextUtils.isEmpty(date) || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime) || TextUtils.isEmpty(description))
    }

    // Save the timesheet entry to Firebase under the selected category
    private fun saveTimesheetEntry(timesheet: Timesheet) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userTimesheetsRef = databaseRef.child("Users").child(userId).child("categories").child(timesheet.category).child("timesheets")
        val timesheetId = userTimesheetsRef.push().key ?: return

        userTimesheetsRef.child(timesheetId).setValue(timesheet)
            .addOnSuccessListener {
                Toast.makeText(this, "Timesheet saved to Firebase.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save timesheet: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}