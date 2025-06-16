package com.example.masakin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {

    private lateinit var detailImage: ImageView
    private lateinit var detailName: TextView
    private lateinit var detailCategory: TextView
    private lateinit var detailArea: TextView
    private lateinit var detailInstructions: TextView
    private lateinit var detailIngredients: TextView
    private lateinit var favoriteButton: Button

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        databaseHelper = DatabaseHelper(this)

        detailImage = findViewById(R.id.detailImage)
        detailName = findViewById(R.id.detailName)
        detailCategory = findViewById(R.id.detailCategory)
        detailArea = findViewById(R.id.detailArea)
        detailInstructions = findViewById(R.id.detailInstructions)
        detailIngredients = findViewById(R.id.detailIngredients)
        favoriteButton = findViewById(R.id.favoriteButton) // <- penting!

        val meal = intent.getSerializableExtra("meal") as? Meal
        meal?.let { displayMealDetails(it) }
    }

    private fun displayMealDetails(meal: Meal) {
        title = meal.name

        Glide.with(this)
            .load(meal.image)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(detailImage)

        detailName.text = meal.name
        detailCategory.text = "Category: ${meal.category}"
        detailArea.text = "Area: ${meal.area}"
        detailInstructions.text = meal.instructions
        detailIngredients.text = "Ingredients:\n${meal.ingredients.joinToString("\n")}"

        favoriteButton.setOnClickListener {
            val added = databaseHelper.insertFavorite(meal.name, meal.image)
            if (added) {
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
