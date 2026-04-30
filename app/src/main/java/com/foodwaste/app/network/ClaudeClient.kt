package com.foodwaste.app.network

import com.foodwaste.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Minimal Anthropic Messages API client. Two endpoints:
 *  - extractReceipt: image -> structured items JSON
 *  - generateRecipes: inventory summary -> recipes JSON
 * Both return the raw text the model produced; callers parse it.
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
     * @param imageBase64 base64-encoded JPEG/PNG bytes (no data: prefix)
     */
    suspend fun extractReceipt(imageBase64: String, mediaType: String = "image/jpeg"): String {
        val body = buildJsonObject {
            put("model", model)
            put("max_tokens", 2048)
            put("system", RECEIPT_SYSTEM_PROMPT)
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
                            put("text", "Parse this receipt.")
                        })
                    })
                })
            })
        }
        return postMessages(body, errorLabel = "Receipt scan")
    }

    /**
     * inventorySummary should be a plain-text bullet list of items + days-until-expiration,
     * sorted soonest-first. count is how many recipes to ask for.
     */
    suspend fun generateRecipes(inventorySummary: String, count: Int = 6): String {
        val body = buildJsonObject {
            put("model", model)
            put("max_tokens", 4096)
            put("system", RECIPE_SYSTEM_PROMPT)
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", "Inventory (sorted by expiration):\n$inventorySummary\n\n" +
                        "Generate $count diverse recipes that prioritize ingredients " +
                        "expiring soonest. Return JSON only.")
                })
            })
        }
        return postMessages(body, errorLabel = "Recipe generation")
    }

    private suspend fun postMessages(body: JsonObject, errorLabel: String): String =
        withContext(Dispatchers.IO) {
            require(apiKey.isNotBlank()) { "API key is not set. Add it to local.properties." }

            val req = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            http.newCall(req).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                check(resp.isSuccessful) { "$errorLabel failed (${resp.code}): $text" }

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
        private const val RECEIPT_SYSTEM_PROMPT = """
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

        private const val RECIPE_SYSTEM_PROMPT = """
You are a recipe generator for a food-waste app. Given the user's current
inventory (with days-until-expiration), generate recipes that prioritize
ingredients expiring soonest. Each recipe should heavily reuse inventory items;
some may need a small number of pantry staples not on hand.

For each recipe:
- id: short snake_case slug, unique within the response
- title: short, appetizing
- ingredients: lower-case singular names. Match inventory item names exactly when possible
  (e.g. "banana" not "bananas", "egg" not "eggs"). Keep the list concise (5-9 items).
- steps: 3-6 short imperative sentences
- minutes: integer total time

Respond with a SINGLE JSON object matching this schema and NOTHING else:
{"recipes":[{"id":"","title":"","ingredients":[""],"steps":[""],"minutes":0}]}
"""
    }
}
