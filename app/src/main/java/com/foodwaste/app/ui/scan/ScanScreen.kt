package com.foodwaste.app.ui.scan

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp

@Composable
fun ScanScreen(vm: ScanViewModel, onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val state by vm.state.collectAsState()
    val ctx = LocalContext.current

    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        pickedUri = uri
        if (uri != null) {
            val bmp = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
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
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }) { Text("Pick receipt image") }

        when (val s = state) {
            is ScanState.Idle -> Text("Pick a grocery receipt photo to begin.")
            is ScanState.Working -> {
                CircularProgressIndicator()
                Text("Extracting items with Claude…")
            }
            is ScanState.Preview -> PreviewList(
                items = s.items.map { it.name + " • " + it.category },
                onConfirm = { vm.confirm(s.items) },
                onCancel = { vm.reset() }
            )
            is ScanState.Error -> {
                Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { vm.reset() }) { Text("Try again") }
            }
            is ScanState.Saved -> Text("Saved!")
        }
    }
}

@Composable
private fun PreviewList(items: List<String>, onConfirm: () -> Unit, onCancel: () -> Unit) {
    Text("${items.size} items detected — confirm to add:")
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { line ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(line, modifier = Modifier.padding(10.dp))
            }
        }
    }
    Button(onClick = onConfirm) { Text("Add to inventory") }
    OutlinedButton(onClick = onCancel) { Text("Cancel") }
}
