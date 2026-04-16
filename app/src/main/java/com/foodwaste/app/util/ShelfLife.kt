package com.foodwaste.app.util

/**
 * Fallback shelf-life (days) when the VLM doesn't return one.
 * These are coarse defaults tied to our category taxonomy.
 */
object ShelfLife {
    fun defaultDaysFor(category: String): Int = when (category.lowercase()) {
        "produce"  -> 7
        "dairy"    -> 10
        "meat"     -> 3
        "seafood"  -> 2
        "bakery"   -> 5
        "frozen"   -> 90
        "pantry"   -> 180
        "beverage" -> 30
        else       -> 7
    }
}
