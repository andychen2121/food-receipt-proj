package com.foodwaste.app.data

/**
 * In v0 we ship a hand-curated recipe list in assets. Swap for an API later.
 */
data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<String>,   // normalized lower-case names
    val steps: List<String>,
    val minutes: Int
)

data class RecipeMatch(
    val recipe: Recipe,
    val have: List<String>,       // inventory items covered
    val missing: List<String>,    // ingredients not in inventory
    val urgencyScore: Double      // higher = uses more near-expiring items
)
