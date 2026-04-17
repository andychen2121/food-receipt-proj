package com.foodwaste.app.data

import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * v0 recipe matcher. Scores recipes by:
 *   - how much of the user's near-expiring inventory they consume
 *   - how few missing ingredients they require
 * Swap for a real API (Spoonacular / Edamam) later.
 */
class RecipeEngine(private val recipes: List<Recipe>) {

    fun recommend(inventory: List<InventoryItem>, now: Long): List<RecipeMatch> {
        val byName = inventory.associateBy { it.name.lowercase() }

        return recipes.map { recipe ->
            val have = mutableListOf<String>()
            val missing = mutableListOf<String>()
            var urgency = 0.0

            for (ing in recipe.ingredients) {
                val key = ing.lowercase()
                val matchKey: String? =
                    if (byName.containsKey(key)) key else partialMatch(key, byName.keys)
                if (matchKey != null) {
                    val item = byName.getValue(matchKey)
                    have += matchKey
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(item.expiresAt - now)
                    // expiring soon => higher urgency; negative = already expired (very urgent)
                    urgency += 1.0 / max(1.0, (daysLeft + 1).toDouble())
                } else {
                    missing += ing
                }
            }
            RecipeMatch(recipe, have, missing, urgency)
        }.sortedWith(
            compareByDescending<RecipeMatch> { it.urgencyScore }
                .thenBy { it.missing.size }
        )
    }

    private fun partialMatch(needle: String, haystack: Set<String>): String? =
        haystack.firstOrNull { it.contains(needle) || needle.contains(it) }

    companion object {
        /** Hardcoded starter set — replace with assets/recipes.json load. */
        val starter = listOf(
            Recipe(
                id = "veggie_stirfry",
                title = "Quick Veggie Stir-Fry",
                ingredients = listOf("broccoli", "bell pepper", "garlic", "soy sauce", "rice"),
                steps = listOf("Chop veg.", "Stir-fry 5 min.", "Add soy sauce.", "Serve over rice."),
                minutes = 15
            ),
            Recipe(
                id = "banana_bread",
                title = "Banana Bread",
                ingredients = listOf("banana", "flour", "sugar", "egg", "butter"),
                steps = listOf("Mash bananas.", "Mix.", "Bake 50 min at 350F."),
                minutes = 60
            ),
            Recipe(
                id = "yogurt_parfait",
                title = "Yogurt Parfait",
                ingredients = listOf("yogurt", "granola", "berries"),
                steps = listOf("Layer in a glass.", "Eat."),
                minutes = 3
            ),
            Recipe(
                id = "omelette",
                title = "Veggie Omelette",
                ingredients = listOf("egg", "spinach", "cheese", "butter"),
                steps = listOf("Beat eggs.", "Cook in butter.", "Add fillings, fold."),
                minutes = 10
            )
        )
    }
}
