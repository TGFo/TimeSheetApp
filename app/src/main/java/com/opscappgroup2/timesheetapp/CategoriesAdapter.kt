package com.opscappgroup2.timesheetapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoriesAdapter(val context : Context,private val categories : MutableList<Category>) : RecyclerView.Adapter<CategoriesAdapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var textName : TextView
        lateinit var textDescription : TextView
        init {
            textName = itemView.findViewById(R.id.nameTextView)
            textDescription = itemView.findViewById(R.id.descriptionTextView)
            itemView.setOnClickListener()
            {

            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoriesAdapter.MyViewHolder {
        var inflater : LayoutInflater = LayoutInflater.from(context)
        var view : View = inflater.inflate(R.layout.activity_item, parent, false)
        return CategoriesAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriesAdapter.MyViewHolder, position: Int) {
        holder.textName.text = categories.get(position).name
        holder.textDescription.text = categories.get(position).description
    }

    override fun getItemCount(): Int {
        return categories.size
    }



}