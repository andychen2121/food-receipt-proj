package com.foodwaste.app

import android.app.Application
import com.foodwaste.app.data.AppDatabase
import com.foodwaste.app.data.InventoryRepository
import com.foodwaste.app.network.ClaudeClient
import com.foodwaste.app.network.ReceiptParser
import com.foodwaste.app.network.RecipeGenerator

/**
 * Manual service locator (keeps v0 simple — swap for Hilt later).
 */
class FoodWasteApplication : Application() {
    lateinit var repo: InventoryRepository
        private set
    lateinit var parser: ReceiptParser
        private set
    lateinit var recipeGenerator: RecipeGenerator
        private set

    override fun onCreate() {
        super.onCreate()
        val dao = AppDatabase.get(this).inventoryDao()
        val client = ClaudeClient()
        repo = InventoryRepository(dao)
        parser = ReceiptParser(client)
        recipeGenerator = RecipeGenerator(client)
    }
}
