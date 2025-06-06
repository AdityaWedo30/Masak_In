package com.example.masakin


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var mealAdapter: MealAdapter
    private val mealList = mutableListOf<Meal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupSearchButton()

        // Load default Chicken recipes
        searchMeal("Chicken")
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        progressBar = findViewById(R.id.progressBar)
        recyclerView = findViewById(R.id.recyclerView)
    }

    private fun setupRecyclerView() {
        mealAdapter = MealAdapter(mealList) { meal ->
            // Handle meal item click
            Toast.makeText(this, "Selected: ${meal.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = mealAdapter
        }
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchMeal(query)
            } else {
                Toast.makeText(this, "Please enter a meal name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchMeal(mealName: String) {
        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiUrl = "https://www.themealdb.com/api/json/v1/1/search.php?s=$mealName"
                val response = makeApiCall(apiUrl)
                val meals = parseMealsFromJson(response)

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    updateMealList(meals)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("MealApp", "Error fetching meals", e)
                }
            }
        }
    }

    private fun makeApiCall(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            reader.close()
            response.toString()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseMealsFromJson(jsonString: String): List<Meal> {
        val meals = mutableListOf<Meal>()

        try {
            val jsonObject = JSONObject(jsonString)
            val mealsArray = jsonObject.optJSONArray("meals")

            if (mealsArray != null) {
                for (i in 0 until mealsArray.length()) {
                    val mealObject = mealsArray.getJSONObject(i)

                    val meal = Meal(
                        id = mealObject.optString("idMeal", ""),
                        name = mealObject.optString("strMeal", ""),
                        category = mealObject.optString("strCategory", ""),
                        area = mealObject.optString("strArea", ""),
                        instructions = mealObject.optString("strInstructions", ""),
                        image = mealObject.optString("strMealThumb", ""),
                        ingredients = getIngredientsFromJson(mealObject)
                    )

                    meals.add(meal)
                }
            }
        } catch (e: Exception) {
            Log.e("MealApp", "Error parsing JSON", e)
        }

        return meals
    }

    private fun getIngredientsFromJson(mealObject: JSONObject): List<String> {
        val ingredients = mutableListOf<String>()

        for (i in 1..20) {
            val ingredient = mealObject.optString("strIngredient$i", "")
            val measure = mealObject.optString("strMeasure$i", "")

            if (ingredient.isNotEmpty() && ingredient.trim() != "null") {
                val fullIngredient = if (measure.isNotEmpty() && measure.trim() != "null") {
                    "$measure $ingredient"
                } else {
                    ingredient
                }
                ingredients.add(fullIngredient.trim())
            }
        }

        return ingredients
    }

    private fun updateMealList(meals: List<Meal>) {
        mealList.clear()
        mealList.addAll(meals)
        mealAdapter.notifyDataSetChanged()

        if (meals.isEmpty()) {
            Toast.makeText(this, "No meals found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        searchButton.isEnabled = !show
    }
}

// Data class untuk Meal
data class Meal(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val image: String,
    val ingredients: List<String>
)

// Adapter untuk RecyclerView
class MealAdapter(
    private val meals: List<Meal>,
    private val onItemClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val mealCategory: TextView = itemView.findViewById(R.id.mealCategory)
        val mealArea: TextView = itemView.findViewById(R.id.mealArea)
        val mealInstructions: TextView = itemView.findViewById(R.id.mealInstructions)
        val ingredientsList: TextView = itemView.findViewById(R.id.ingredientsList)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MealViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]

        holder.mealName.text = meal.name
        holder.mealCategory.text = "Category: ${meal.category}"
        holder.mealArea.text = "Area: ${meal.area}"
        holder.mealInstructions.text = meal.instructions
        holder.ingredientsList.text = "Ingredients:\n${meal.ingredients.joinToString("\n")}"

        // Load image using Glide (you'll need to add Glide dependency)
        Glide.with(holder.itemView.context)
            .load(meal.image)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(holder.mealImage)

        holder.itemView.setOnClickListener {
            onItemClick(meal)
        }
    }

    override fun getItemCount(): Int = meals.size
}