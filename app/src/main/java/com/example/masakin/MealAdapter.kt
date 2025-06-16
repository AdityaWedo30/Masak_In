package com.example.masakin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MealAdapter(
        private val meals: List<Meal>,
        private val onItemClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
    val mealName: TextView = itemView.findViewById(R.id.mealName)
    val mealCategory: TextView = itemView.findViewById(R.id.mealCategory)
    val mealArea: TextView = itemView.findViewById(R.id.mealArea)
    val mealInstructions: TextView = itemView.findViewById(R.id.mealInstructions)
    val ingredientsList: TextView = itemView.findViewById(R.id.ingredientsList)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_card, parent, false)
    return MealViewHolder(view)
}

override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
    val meal = meals[position]

    holder.mealName.text = meal.name
    holder.mealCategory.text = "Category: ${meal.category}"
    holder.mealArea.text = "Area: ${meal.area}"
    holder.mealInstructions.text = meal.instructions
    holder.ingredientsList.text = "Ingredients:\n${meal.ingredients.joinToString("\n")}"

    Glide.with(holder.itemView.context)
            .load(meal.image)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.mealImage)

    holder.itemView.setOnClickListener {
        onItemClick(meal)
    }
}

override fun getItemCount(): Int = meals.size
}
