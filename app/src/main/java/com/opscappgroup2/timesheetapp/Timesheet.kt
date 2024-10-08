package com.opscappgroup2.timesheetapp

data class Timesheet(
    val date: String,
    val startTime: String,
    val endTime: String,
    val description: String,
    val photoUri: String? = null,
    val category: String
)