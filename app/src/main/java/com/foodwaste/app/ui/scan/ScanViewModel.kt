package com.foodwaste.app.ui.scan

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodwaste.app.data.InventoryRepository
import com.foodwaste.app.network.ParsedReceiptItem
import com.foodwaste.app.network.ReceiptParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

sealed interface ScanState {
    data object Idle : ScanState
    data object Working : ScanState
    data class Preview(val items: List<ParsedReceiptItem>) : ScanState
    data class Error(val message: String) : ScanState
    data object Saved : ScanState
}

class ScanViewModel(
    private val parser: ReceiptParser,
    private val repo: InventoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    fun scan(bitmap: Bitmap) = viewModelScope.launch {
        _state.value = ScanState.Working
        try {
            val b64 = bitmap.toJpegBase64()
            val parsed = parser.parse(b64, "image/jpeg")
            _state.value = ScanState.Preview(parsed.items)
        } catch (t: Throwable) {
            _state.value = ScanState.Error(t.message ?: "Failed to parse receipt")
        }
    }

    fun confirm(items: List<ParsedReceiptItem>) = viewModelScope.launch {
        repo.addParsedItems(items, System.currentTimeMillis())
        _state.value = ScanState.Saved
    }

    fun reset() { _state.value = ScanState.Idle }

    private fun Bitmap.toJpegBase64(quality: Int = 85): String {
        val out = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }
}
