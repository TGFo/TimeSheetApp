package com.opscappgroup2.timesheetapp
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

@Suppress("DEPRECATION")
class TimesheetsCreateActivity : AppCompatActivity() {

    private lateinit var photoImageView: ImageView
    private var selectedImageUri: Uri? = null
    private var selectedCategory: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userId: String
    private val timesheets = mutableListOf<Timesheet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_create)

        // Initialize FirebaseAuth to get the current user
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userId = currentUser?.uid ?: "default_user"

        // Initialize SharedPreferences (specific to timesheets)
        sharedPreferences = getSharedPreferences("UserTimesheets", Context.MODE_PRIVATE)

        // Load existing categories for the user
        val categories = loadCategories()

        val dateEditText: EditText = findViewById(R.id.dateEditText)
        val startTimeEditText: EditText = findViewById(R.id.startTimeEditText)
        val endTimeEditText: EditText = findViewById(R.id.endTimeEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val categorySpinner: Spinner = findViewById(R.id.categorySpinner)
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        photoImageView = findViewById(R.id.photoImageView)
        val saveButton: Button = findViewById(R.id.saveButton)

        // Set up the category spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        // Set date picker for the date field
        dateEditText.setOnClickListener {
            showDatePickerDialog { date ->
                dateEditText.setText(date)
            }
        }

        // Set time picker for the start time
        startTimeEditText.setOnClickListener {
            showTimePickerDialog { time ->
                startTimeEditText.setText(time)
            }
        }

        // Set time picker for the end time
        endTimeEditText.setOnClickListener {
            showTimePickerDialog { time ->
                endTimeEditText.setText(time)
            }
        }

        // Category selection
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
            }
        }

        // Handle adding a photo
        addPhotoButton.setOnClickListener {
            openGalleryForImage()
        }

        // Handle saving the timesheet entry
        saveButton.setOnClickListener {
            val date = dateEditText.text.toString()
            val startTime = startTimeEditText.text.toString()
            val endTime = endTimeEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val category = selectedCategory

            if (!validateFields(date, startTime, endTime, description)) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the timesheet entry locally
            saveTimesheetEntry(
                Timesheet(
                    date, startTime, endTime, description,
                    selectedImageUri?.toString(), category.toString()
                )
            )
        }
    }

    // Show Date Picker dialog for date selection
    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            onDateSelected(date)
        }, year, month, day)

        datePickerDialog.show()
    }

    // Show Time Picker dialog for time selection
    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun validateFields(
        date: String,
        startTime: String,
        endTime: String,
        description: String
    ): Boolean {
        return !(TextUtils.isEmpty(date) || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime) || TextUtils.isEmpty(description))
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            photoImageView.setImageURI(selectedImageUri)
            photoImageView.visibility = View.VISIBLE
        }
    }

    // Load categories for the current user from SharedPreferences
    private fun loadCategories(): List<String> {
        val categoryPrefs = getSharedPreferences("UserCategories", Context.MODE_PRIVATE)
        val jsonCategories = categoryPrefs.getString(userId, null)
        return if (!jsonCategories.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<Category>>() {}.type
            val savedCategories: MutableList<Category> = gson.fromJson(jsonCategories, type)
            savedCategories.map { it.name }
        } else {
            emptyList()
        }
    }

    private fun saveTimesheetEntry(timesheet: Timesheet) {
        val jsonTimesheets = sharedPreferences.getString(userId + "_timesheets", null)
        val gson = Gson()
        val type = object : TypeToken<MutableList<Timesheet>>() {}.type
        val savedTimesheets: MutableList<Timesheet> = if (!jsonTimesheets.isNullOrEmpty()) {
            gson.fromJson(jsonTimesheets, type)
        } else {
            mutableListOf()
        }

        savedTimesheets.add(timesheet)

        val jsonUpdatedTimesheets = gson.toJson(savedTimesheets)
        sharedPreferences.edit().putString(userId + "_timesheets", jsonUpdatedTimesheets).apply()

        Toast.makeText(this, "Timesheet entry saved", Toast.LENGTH_SHORT).show()

        finish()
    }

    companion object {
        const val GALLERY_REQUEST_CODE = 123
    }
}