package com.foodwaste.app.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodwaste.app.data.InventoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun InventoryScreen(vm: InventoryViewModel, modifier: Modifier = Modifier) {
    val items by vm.items.collectAsState()

    if (items.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No items yet.", style = MaterialTheme.typography.titleMedium)
            Text("Scan a receipt to get started.", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            ItemRow(
                item = item,
                onConsumed = { vm.markConsumed(item.id) },
                onDelete = { vm.delete(item.id) }
            )
        }
    }
}

@Composable
private fun ItemRow(item: InventoryItem, onConsumed: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${item.category} • ${daysLeftLabel(item.expiresAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onConsumed) {
                Icon(Icons.Default.Check, contentDescription = "Mark used up")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

private fun daysLeftLabel(expiresAt: Long): String {
    val now = System.currentTimeMillis()
    val days = TimeUnit.MILLISECONDS.toDays(expiresAt - now)
    return when {
        days < 0 -> "expired ${-days}d ago"
        days == 0L -> "expires today"
        days == 1L -> "1 day left"
        else -> "$days days left"
    } + " (${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(expiresAt))})"
}
