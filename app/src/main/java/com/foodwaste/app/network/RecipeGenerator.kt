package com.foodwaste.app.network

import com.foodwaste.app.data.InventoryItem
import com.foodwaste.app.data.Recipe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Asks the LLM for inventory-driven recipes and decodes the JSON into our Recipe model.
 */
class RecipeGenerator(private val client: ClaudeClient = ClaudeClient()) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun generate(inventory: List<InventoryItem>, count: Int = 6): List<Recipe> {
        require(inventory.isNotEmpty()) {
            "Add some items to your inventory first — generated recipes are based on what you have."
        }
        val raw = client.generateRecipes(inventory.toSummary(), count)
        val clean = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()
        return json.decodeFromString(GeneratedRecipes.serializer(), clean).recipes
            .map { it.toRecipe() }
    }

    private fun List<InventoryItem>.toSummary(): String {
        val now = System.currentTimeMillis()
        return joinToString("\n") { item ->
            val daysLeft = TimeUnit.MILLISECONDS.toDays(item.expiresAt - now)
            val tag = when {
                daysLeft < 0 -> "expired"
                daysLeft == 0L -> "today"
                else -> "${daysLeft}d left"
            }
            "- ${item.name} (${item.category}, $tag)"
        }
    }

    @Serializable
    private data class GeneratedRecipes(val recipes: List<GeneratedRecipe>)

    @Serializable
    private data class GeneratedRecipe(
        val id: String,
        val title: String,
        val ingredients: List<String>,
        val steps: List<String>,
        val minutes: Int
    ) {
        fun toRecipe() = Recipe(
            id = id,
            title = title,
            ingredients = ingredients.map { it.lowercase().trim() },
            steps = steps,
            minutes = minutes
        )
    }
}
