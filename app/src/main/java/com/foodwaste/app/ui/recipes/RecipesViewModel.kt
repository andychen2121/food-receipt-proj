package com.foodwaste.app.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodwaste.app.data.InventoryRepository
import com.foodwaste.app.data.Recipe
import com.foodwaste.app.data.RecipeEngine
import com.foodwaste.app.data.RecipeMatch
import com.foodwaste.app.network.RecipeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GenState {
    data object Idle : GenState
    data object Loading : GenState
    data class Error(val message: String) : GenState
}

class RecipesViewModel(
    private val repo: InventoryRepository,
    private val generator: RecipeGenerator
) : ViewModel() {

    /** Current recipe set. Starts with the hand-curated starter list, replaced on Generate. */
    private val recipes = MutableStateFlow(RecipeEngine.starter)

    private val _genState = MutableStateFlow<GenState>(GenState.Idle)
    val genState: StateFlow<GenState> = _genState.asStateFlow()

    /** Reactive: re-runs when inventory or recipe set changes. */
    val matches: StateFlow<List<RecipeMatch>> = combine(
        repo.observeActive(),
        recipes
    ) { inv, rcps ->
        RecipeEngine(rcps).recommend(inv, System.currentTimeMillis())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun generate() = viewModelScope.launch {
        _genState.value = GenState.Loading
        try {
            val inv = repo.snapshot()
            val fresh: List<Recipe> = generator.generate(inv)
            if (fresh.isEmpty()) error("No recipes returned.")
            recipes.value = fresh
            _genState.value = GenState.Idle
        } catch (t: Throwable) {
            _genState.value = GenState.Error(t.message ?: "Failed to generate recipes")
        }
    }

    fun dismissError() { _genState.value = GenState.Idle }
}
