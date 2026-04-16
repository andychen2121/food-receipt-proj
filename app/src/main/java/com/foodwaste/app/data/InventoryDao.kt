package com.foodwaste.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items WHERE consumed = 0 ORDER BY expiresAt ASC")
    fun observeActive(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE consumed = 0 ORDER BY expiresAt ASC")
    suspend fun getActive(): List<InventoryItem>

    @Insert
    suspend fun insertAll(items: List<InventoryItem>): List<Long>

    @Update
    suspend fun update(item: InventoryItem)

    @Query("UPDATE inventory_items SET consumed = 1 WHERE id = :id")
    suspend fun markConsumed(id: Long)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun delete(id: Long)
}
