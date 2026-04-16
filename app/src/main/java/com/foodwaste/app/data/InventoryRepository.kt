package com.foodwaste.app.data

import com.foodwaste.app.network.ParsedReceiptItem
import com.foodwaste.app.util.ShelfLife
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class InventoryRepository(private val dao: InventoryDao) {

    fun observeActive(): Flow<List<InventoryItem>> = dao.observeActive()

    suspend fun addParsedItems(parsed: List<ParsedReceiptItem>, purchasedAt: Long) {
        val items = parsed.map {
            val days = it.shelfLifeDays ?: ShelfLife.defaultDaysFor(it.category)
            InventoryItem(
                name = it.name,
                category = it.category,
                quantity = it.quantity,
                purchasedAt = purchasedAt,
                expiresAt = purchasedAt + TimeUnit.DAYS.toMillis(days.toLong())
            )
        }
        dao.insertAll(items)
    }

    suspend fun overrideExpiration(item: InventoryItem, newExpiresAt: Long) {
        dao.update(item.copy(expiresAt = newExpiresAt, manuallyOverridden = true))
    }

    suspend fun markConsumed(id: Long) = dao.markConsumed(id)
    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun snapshot(): List<InventoryItem> = dao.getActive()
}
