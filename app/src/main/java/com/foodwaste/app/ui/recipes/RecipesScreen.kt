package com.foodwaste.app.ui.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodwaste.app.data.RecipeMatch

@Composable
fun RecipesScreen(vm: RecipesViewModel, modifier: Modifier = Modifier) {
    val matches by vm.matches.collectAsState()
    val genState by vm.genState.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        GenerateBar(
            genState = genState,
            onGenerate = vm::generate,
            onDismissError = vm::dismissError
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(matches) { RecipeCard(it) }
        }
    }
}

@Composable
private fun GenerateBar(
    genState: GenState,
    onGenerate: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recipes", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onGenerate, enabled = genState !is GenState.Loading) {
                if (genState is GenState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                }
                Text(
                    text = if (genState is GenState.Loading) "  Generating…" else "  Generate",
                )
            }
        }
        if (genState is GenState.Error) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = genState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    OutlinedButton(onClick = onDismissError) { Text("Dismiss") }
                }
            }
        }
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
