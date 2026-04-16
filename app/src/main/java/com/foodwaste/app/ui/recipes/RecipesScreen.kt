package com.foodwaste.app.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodwaste.app.data.RecipeMatch

@Composable
fun RecipesScreen(vm: RecipesViewModel, modifier: Modifier = Modifier) {
    val matches by vm.matches.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(matches) { RecipeCard(it) }
    }
}

@Composable
private fun RecipeCard(match: RecipeMatch) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(match.recipe.title, style = MaterialTheme.typography.titleMedium)
            Text("${match.recipe.minutes} min", style = MaterialTheme.typography.bodySmall)
            Text(
                "Uses: ${if (match.have.isEmpty()) "—" else match.have.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (match.missing.isNotEmpty()) {
                Text(
                    "Missing: ${match.missing.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { /* TODO: cook with existing */ }) {
                        Text("Cook with what I have")
                    }
                    OutlinedButton(onClick = { /* TODO: generate shopping list */ }) {
                        Text("Shopping list")
                    }
                }
            }
        }
    }
}
