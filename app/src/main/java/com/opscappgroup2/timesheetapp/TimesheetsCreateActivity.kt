package com.opscappgroup2.timesheetapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import java.text.SimpleDateFormat
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


    private var selectedImageUri: Uri? = null
    private var selectedCategoryId: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private val timesheets = mutableListOf<Timesheet>()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var categorySpinner: Spinner
    private lateinit var startTimeTextView: TextView
    private lateinit var endTimeTextView: TextView
    private val categoryMap = mutableMapOf<String, String>() // Map to store category names and IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_create)

        // Initialize FirebaseAuth and get user ID
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: "default_user"


        val dateEditText: EditText = findViewById(R.id.dateEditText)
        startTimeTextView = findViewById(R.id.startTimeTextView)
        endTimeTextView = findViewById(R.id.endTimeTextView)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        val saveButton: Button = findViewById(R.id.saveButton)

        // Set up image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data?.data

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

        startTimeTextView.setOnClickListener {
            showTimePickerDialog { time -> startTimeTextView.text = time }
        }

        endTimeTextView.setOnClickListener {
            showTimePickerDialog { time -> endTimeTextView.text = time }
        }


        // Handle saving the timesheet entry
        saveButton.setOnClickListener {
            val date = dateEditText.text.toString()
            val startTime = startTimeTextView.text.toString()
            val endTime = endTimeTextView.text.toString()
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
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(time)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    // Validate input fields
    private fun validateFields(date: String, startTime: String, endTime: String, description: String): Boolean {
        return !(TextUtils.isEmpty(date) || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime) || TextUtils.isEmpty(description))
    }

    // Save the timesheet entry to Firebase under the selected category
    private fun saveTimesheetEntry(timesheet: Timesheet) {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userRef = databaseRef.child("Users").child(userId)
        val userTimesheetsRef = databaseRef.child("Users").child(userId).child("categories").child(timesheet.category).child("timesheets")
        val timesheetId = userTimesheetsRef.push().key ?: return

        userTimesheetsRef.child(timesheetId).setValue(timesheet)
            .addOnSuccessListener {
                calculateChuddiePoints(timesheet)
                Toast.makeText(this, "Timesheet saved to Firebase.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save timesheet: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun calculateChuddiePoints(timesheet: Timesheet) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
        val goalsRef = userRef.child("hourGoals")

        // Fetch minGoal and maxGoal from Firebase
        goalsRef.get().addOnSuccessListener { snapshot ->
            val minGoal = snapshot.child("minGoal").getValue(Float::class.java) ?: 0f
            val maxGoal = snapshot.child("maxGoal").getValue(Float::class.java) ?: 0f

            if (minGoal == 0f || maxGoal == 0f || minGoal >= maxGoal) {
                Toast.makeText(this, "Invalid goals data. Please set your goals.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Parse start and end time from the timesheet
            val startTime = timesheet.startTime?.toTime()
            val endTime = timesheet.endTime?.toTime()

            if (startTime == null || endTime == null || !endTime.after(startTime)) {
                Toast.makeText(this, "Invalid start or end time.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Calculate hours worked
            val hoursWorked = (endTime.time - startTime.time) / (1000.0 * 60 * 60)
            var pointsEarned = 0

            // Determine points based on hours worked
            val range = maxGoal - minGoal
            when {
                hoursWorked >= maxGoal -> pointsEarned = 5
                hoursWorked >= minGoal + range * 0.75 -> pointsEarned = 4
                hoursWorked >= minGoal + range * 0.5 -> pointsEarned = 3
                hoursWorked >= minGoal + range * 0.25 -> pointsEarned = 2
                hoursWorked >= minGoal -> pointsEarned = 1
            }

            // Update Chuddie Points
            updateChuddiePoints(pointsEarned)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to fetch goals: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateChuddiePoints(pointsEarned: Int) {
        val userPointsRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("chuddiePoints")

        userPointsRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.getValue(Int::class.java) ?: 0
            val newPoints = currentPoints + pointsEarned

            // Update points in Firebase
            userPointsRef.setValue(newPoints).addOnSuccessListener {
                Toast.makeText(this, "You earned $pointsEarned points! Total points: $newPoints", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update points: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to fetch current points: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    fun String.toTime(): Date? {
        return try {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeFormat.isLenient = false
            timeFormat.parse(this)
        } catch (e: Exception) {
            null
        }
    }
}