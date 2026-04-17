package com.foodwaste.app.network

import com.foodwaste.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Minimal Anthropic Messages API client. Sends an image + prompt and
 * expects a JSON object in the assistant text block.
 */
class ClaudeClient(
    private val apiKey: String = BuildConfig.CLAUDE_API_KEY,
    private val model: String = "claude-haiku-4-5-20251001"
) {
    private val http = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Returns the raw JSON text the model produced (caller parses it).
     * @param imageBase64 base64-encoded JPEG/PNG bytes (no data: prefix)
     */
    suspend fun extractReceipt(imageBase64: String, mediaType: String = "image/jpeg"): String =
        withContext(Dispatchers.IO) {
            require(apiKey.isNotBlank()) { "CLAUDE_API_KEY is not set. Add it to local.properties." }

            val body = buildJsonObject {
                put("model", model)
                put("max_tokens", 2048)
                put("system", SYSTEM_PROMPT)
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", buildJsonArray {
                            add(buildJsonObject {
                                put("type", "image")
                                put("source", buildJsonObject {
                                    put("type", "base64")
                                    put("media_type", mediaType)
                                    put("data", imageBase64)
                                })
                            })
                            add(buildJsonObject {
                                put("type", "text")
                                put("text", USER_PROMPT)
                            })
                        })
                    })
                })
            }.toString()

            val req = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            http.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                check(resp.isSuccessful) { "Claude API ${resp.code}: $text" }

                // Response shape: { content: [ { type: "text", text: "..." }, ... ] }
                val root = json.parseToJsonElement(text).jsonObject
                val content = root["content"] as? JsonArray ?: error("no content")
                val textBlock = content
                    .map { it.jsonObject }
                    .firstOrNull { it["type"]?.jsonPrimitive?.content == "text" }
                    ?: error("no text block in response")
                textBlock["text"]?.jsonPrimitive?.content ?: error("empty text block")
            }
        }

    companion object {
        private const val SYSTEM_PROMPT = """
You are a receipt parser for a food-inventory app. Given an image of a grocery
receipt, extract every purchased food item. For each item output:
- name: a clean, human-readable name (drop SKU codes, abbreviations expanded)
- category: one of [produce, dairy, meat, seafood, bakery, frozen, pantry, beverage, other]
- quantity: optional, as shown on the receipt (e.g. "2", "1 lb")
- shelfLifeDays: estimated refrigerated shelf life from purchase date; null if unsure
- confidence: 0.0 - 1.0

Ignore non-food lines (taxes, totals, bags, deposits).
Respond with a SINGLE JSON object matching this schema and nothing else:

{"items":[{"name":"","category":"","quantity":null,"shelfLifeDays":null,"confidence":0.0}],"storeName":null,"purchasedAtIso":null}
"""
        private const val USER_PROMPT = "Parse this receipt."
    }
}
