package com.opscappgroup2.timesheetapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(
    private val context: Context,
    private val categories: MutableList<Category>,
    private val onDeleteCategory: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.nameTextView)
        val textDescription: TextView = itemView.findViewById(R.id.descriptionTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.activity_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val category = categories[position]
        holder.textName.text = category.name
        holder.textDescription.text = category.description

        holder.deleteButton.setOnClickListener {
            Toast.makeText(context, "${category.name} deleted", Toast.LENGTH_SHORT).show()
            onDeleteCategory(category)
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(context, TimesheetsActivity::class.java)
            intent.putExtra("categoryId", category.id)
            intent.putExtra("categoryName", category.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}