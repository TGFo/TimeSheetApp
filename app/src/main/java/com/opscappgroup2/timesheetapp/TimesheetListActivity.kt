package com.opscappgroup2.timesheetapp
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class TimesheetListActivity : AppCompatActivity() {

    private lateinit var selectStartDateButton: Button
    private lateinit var selectEndDateButton: Button
    private lateinit var startDateTextView: TextView
    private lateinit var endDateTextView: TextView
    private lateinit var timesheetRecyclerView: RecyclerView
    private lateinit var backToNavigationButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private val timesheets = mutableListOf<Timesheet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timesheets_list)


        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userId = currentUser?.uid ?: "default_user"

        sharedPreferences = getSharedPreferences("UserTimesheets", Context.MODE_PRIVATE)

        selectStartDateButton = findViewById(R.id.selectStartDateButton)
        selectEndDateButton = findViewById(R.id.selectEndDateButton)
        startDateTextView = findViewById(R.id.startDateTextView)
        endDateTextView = findViewById(R.id.endDateTextView)
        timesheetRecyclerView = findViewById(R.id.timesheetRecyclerView)
        backToNavigationButton = findViewById(R.id.backToNavigationButton)

        timesheetRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = TimesheetsAdapter(timesheets)
        timesheetRecyclerView.adapter = adapter

        backToNavigationButton.setOnClickListener {
            finish()
        }

        selectStartDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                startDateTextView.text = date
                filterTimesheetsByDate(adapter)
            }
        }

        selectEndDateButton.setOnClickListener {
            showDatePickerDialog { date ->
                endDateTextView.text = date
                filterTimesheetsByDate(adapter)
            }
        }

        loadTimesheets(adapter)
    }

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

    private fun filterTimesheetsByDate(adapter: TimesheetsAdapter) {
        val startDateString = startDateTextView.text.toString()
        val endDateString = endDateTextView.text.toString()

        if (startDateString != "Select Start Date" && endDateString != "Select End Date") {
            val startDate = startDateString.toDate()
            val endDate = endDateString.toDate()

            if (startDate != null && endDate != null) {
                val filteredTimesheets = timesheets.filter {
                    val timesheetDate = it.date.toDate()
                    timesheetDate != null && (timesheetDate == startDate || timesheetDate == endDate ||
                            (timesheetDate.after(startDate) && timesheetDate.before(endDate)))
                }

                adapter.updateTimesheets(filteredTimesheets)
            }
        }
    }

    private fun loadTimesheets(adapter: TimesheetsAdapter) {
        val jsonTimesheets = sharedPreferences.getString(userId + "_timesheets", null)
        if (!jsonTimesheets.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<Timesheet>>() {}.type
            val savedTimesheets: MutableList<Timesheet> = gson.fromJson(jsonTimesheets, type)

            timesheets.clear()
            timesheets.addAll(savedTimesheets)
            adapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "No timesheets found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun String.toDate(): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    private fun openPhoto(timesheet: Timesheet) {
        if (timesheet.photoUri != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(timesheet.photoUri), "image/*")
            startActivity(intent)
        } else {
            Toast.makeText(this, "No photo available for this timesheet", Toast.LENGTH_SHORT).show()
        }
    }
}