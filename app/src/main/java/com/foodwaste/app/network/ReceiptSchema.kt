package com.foodwaste.app.network

import kotlinx.serialization.Serializable

/**
 * What we ask the VLM to return for each line on a receipt.
 *
 * Categories (closed set): produce, dairy, meat, seafood, bakery, frozen,
 * pantry, beverage, other.
 */
@Serializable
data class ParsedReceiptItem(
    val name: String,
    val category: String,
    val quantity: String? = null,
    val shelfLifeDays: Int? = null,
    val confidence: Double? = null
)

@Serializable
data class ParsedReceipt(
    val items: List<ParsedReceiptItem>,
    val storeName: String? = null,
    val purchasedAtIso: String? = null
)
