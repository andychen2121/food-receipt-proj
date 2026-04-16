package com.foodwaste.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,           // produce, dairy, meat, pantry, frozen, bakery, other
    val quantity: String? = null,   // e.g. "2", "1 lb"
    val purchasedAt: Long,          // epoch millis
    val expiresAt: Long,            // epoch millis (estimated)
    val manuallyOverridden: Boolean = false,
    val consumed: Boolean = false
)
