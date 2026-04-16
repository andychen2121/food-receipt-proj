package com.foodwaste.app.network

import kotlinx.serialization.json.Json

/**
 * Wraps ClaudeClient and converts its raw text into a ParsedReceipt.
 * The VLM is asked to return pure JSON, but we defensively strip code fences
 * in case it wraps the output.
 */
class ReceiptParser(private val client: ClaudeClient = ClaudeClient()) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun parse(imageBase64: String, mediaType: String = "image/jpeg"): ParsedReceipt {
        val raw = client.extractReceipt(imageBase64, mediaType)
        val clean = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()
        return json.decodeFromString(ParsedReceipt.serializer(), clean)
    }
}
