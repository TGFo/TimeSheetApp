package com.opscappgroup2.timesheetapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimesheetsAdapter(private val timesheets: List<Timesheet>) :
    RecyclerView.Adapter<TimesheetsAdapter.TimesheetViewHolder>() {

    class TimesheetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val startTimeTextView: TextView = itemView.findViewById(R.id.startTimeTextView)
        val endTimeTextView: TextView = itemView.findViewById(R.id.endTimeTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimesheetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timesheet_item, parent, false)
        return TimesheetViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimesheetViewHolder, position: Int) {
        val timesheet = timesheets[position]
        holder.dateTextView.text = timesheet.date
        holder.startTimeTextView.text = timesheet.startTime
        holder.endTimeTextView.text = timesheet.endTime
        holder.descriptionTextView.text = timesheet.description
    }

    override fun getItemCount(): Int {
        return timesheets.size
    }
}