package com.opscappgroup2.timesheetapp
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import java.util.Calendar

@Suppress("DEPRECATION")
class TimesheetsCreateActivity : AppCompatActivity() {

    private lateinit var photoImageView: ImageView
    private var selectedImageUri: Uri? = null
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_create)

        val dateEditText: EditText = findViewById(R.id.dateEditText)
        val startTimeEditText: EditText = findViewById(R.id.startTimeEditText)
        val endTimeEditText: EditText = findViewById(R.id.endTimeEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val categorySpinner: Spinner = findViewById(R.id.categorySpinner)
        val addPhotoButton: Button = findViewById(R.id.addPhotoButton)
        photoImageView = findViewById(R.id.photoImageView)
        val saveButton: Button = findViewById(R.id.saveButton)

        val categories = getAvailableCategories()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        dateEditText.setOnClickListener {
            showDatePickerDialog { date ->
                dateEditText.setText(date)
            }
        }
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCategory = categories[position] // Get the selected category
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null // No category selected
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

            // Now, you can save this timesheet entry and optionally include the photo
            saveTimesheetEntry(date, startTime, endTime, description,
                category.toString(), selectedImageUri)
        }
    }

        private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date as a string
                val date = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                onDateSelected(date)
            }, year, month, day)

            datePickerDialog.show()
        }
    private fun validateFields(
        date: String,
        startTime: String,
        endTime: String,
        description: String
    ): Boolean {
        return !(TextUtils.isEmpty(date) || TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime) || TextUtils.isEmpty(description))
    }

    // Open gallery to select an image
    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    // Handle result from image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            photoImageView.setImageURI(selectedImageUri)
            photoImageView.visibility = View.VISIBLE
        }
    }
    private fun getAvailableCategories(): List<String> {
        return listOf("Work", "Project A", "Meeting", "Study", "Personal")
    }
    private fun saveTimesheetEntry(
        date: String,
        startTime: String,
        endTime: String,
        description: String,
        category: String,
        photoUri: Uri?
    ) {
        // Save the timesheet entry to your data source, such as a database or file
        // Optionally include the photoUri if the user added a photograph
        Toast.makeText(this, "Timesheet entry saved", Toast.LENGTH_SHORT).show()

        // Once saved, finish the activity
        finish()
    }

    companion object {
        const val GALLERY_REQUEST_CODE = 123
    }
}