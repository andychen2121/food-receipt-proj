package com.foodwaste.app.ui.scan

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun ScanScreen(vm: ScanViewModel, onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bmp = ctx.contentResolver.openInputStream(uri)
                ?.use { BitmapFactory.decodeStream(it) }
            if (bmp != null) vm.scan(bmp)
        }
    }

    LaunchedEffect(state) { if (state is ScanState.Saved) onSaved() }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scan a receipt", style = MaterialTheme.typography.titleLarge)

        Button(onClick = {
            picker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }) { Text("Pick receipt image") }

        when (val s = state) {
            is ScanState.Idle -> Text("Pick a grocery receipt photo to begin.")

            is ScanState.Working -> {
                CircularProgressIndicator()
                Text("Scanning receipt…")
            }

            is ScanState.Preview -> {
                // Tracks indices the user has excluded. Resets when scan returns new items.
                var excluded by remember(s.items) { mutableStateOf(emptySet<Int>()) }
                val keptCount = s.items.size - excluded.size

                Text(
                    "Tap an item to remove it.",
                    style = MaterialTheme.typography.bodyMedium
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(s.items) { idx, item ->
                        val isExcluded = idx in excluded
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    excluded = if (isExcluded) excluded - idx else excluded + idx
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isExcluded)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textDecoration = if (isExcluded)
                                            TextDecoration.LineThrough else null
                                    )
                                    Text(
                                        item.category,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Icon(
                                    imageVector = if (isExcluded) Icons.Default.Add
                                                  else Icons.Default.Close,
                                    contentDescription = if (isExcluded) "Add back" else "Remove"
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val kept = s.items.filterIndexed { idx, _ -> idx !in excluded }
                        vm.confirm(kept)
                    },
                    enabled = keptCount > 0
                ) { Text("Add $keptCount to inventory") }

                OutlinedButton(onClick = { vm.reset() }) { Text("Cancel") }
            }

            is ScanState.Error -> {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { vm.reset() }) { Text("Try again") }
            }

            is ScanState.Saved -> Text("Saved!")
        }
    }
}
