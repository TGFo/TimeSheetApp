package com.opscappgroup2.timesheetapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

class CetegoriesActivity : AppCompatActivity() {
    val categories = mutableListOf<Category>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cetegories)
        var recyclerView : RecyclerView = findViewById(R.id.categoriesRecyclerView)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        SetUpCategories()
        var adapter : CategoriesAdapter = CategoriesAdapter(this, categories)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    fun SetUpCategories()
    {
        val testNames = mutableListOf("1", "2", "3", "4", "5")
        val testDescriptions = mutableListOf("desc1", "desc2", "desc3", "desc4", "desc5")
        for (i in testNames.indices)
        {
            categories.add(Category(testNames[i],testDescriptions[i]))
        }
    }
}