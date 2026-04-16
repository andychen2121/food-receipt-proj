package com.foodwaste.app.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodwaste.app.data.InventoryRepository
import com.foodwaste.app.data.RecipeEngine
import com.foodwaste.app.data.RecipeMatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipesViewModel(
    private val repo: InventoryRepository,
    private val engine: RecipeEngine = RecipeEngine(RecipeEngine.starter)
) : ViewModel() {

    private val _matches = MutableStateFlow<List<RecipeMatch>>(emptyList())
    val matches: StateFlow<List<RecipeMatch>> = _matches.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        val inv = repo.snapshot()
        _matches.value = engine.recommend(inv, System.currentTimeMillis())
    }
}
