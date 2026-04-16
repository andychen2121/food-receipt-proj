package com.foodwaste.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodwaste.app.data.InventoryItem
import com.foodwaste.app.data.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(private val repo: InventoryRepository) : ViewModel() {

    val items: StateFlow<List<InventoryItem>> =
        repo.observeActive().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun overrideExpiration(item: InventoryItem, newExpiresAt: Long) = viewModelScope.launch {
        repo.overrideExpiration(item, newExpiresAt)
    }

    fun markConsumed(id: Long) = viewModelScope.launch { repo.markConsumed(id) }

    fun delete(id: Long) = viewModelScope.launch { repo.delete(id) }
}
