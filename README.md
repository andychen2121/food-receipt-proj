# FoodWaste (Android, v0)

Kotlin + Jetpack Compose scaffold for the group project. Receipt → VLM → inventory → recipe recommendations prioritizing soon-to-spoil items.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Navigation-Compose (3-tab bottom nav: Inventory / Scan / Recipes)
- Room (local inventory DB)
- OkHttp + kotlinx.serialization (Claude Messages API)
- No OCR library — the VLM does extraction, categorization, and shelf-life estimation in one call

## Project layout

```
app/src/main/java/com/foodwaste/app/
├── MainActivity.kt               # NavHost + bottom bar
├── FoodWasteApplication.kt       # service locator (DB, repo, parser)
├── data/
│   ├── InventoryItem.kt          # Room @Entity
│   ├── InventoryDao.kt
│   ├── AppDatabase.kt
│   ├── InventoryRepository.kt
│   ├── Recipe.kt                 # Recipe / RecipeMatch models
│   └── RecipeEngine.kt           # urgency-weighted matcher + starter recipes
├── network/
│   ├── ReceiptSchema.kt          # ParsedReceipt / ParsedReceiptItem
│   ├── ClaudeClient.kt           # POST /v1/messages with base64 image
│   └── ReceiptParser.kt          # client → typed ParsedReceipt
├── ui/
│   ├── inventory/                # screen + viewmodel
│   ├── scan/                     # image pick → VLM → preview → save
│   └── recipes/                  # ranked RecipeMatch list
└── util/ShelfLife.kt             # fallback days-per-category
```

## Setup

1. `cp local.properties.example local.properties`
2. Fill in `sdk.dir` and `CLAUDE_API_KEY`
3. Open in Android Studio Koala+ (AGP 8.5, Kotlin 1.9.24, minSdk 26)
4. Run on a device/emulator with internet

## The VLM call (the part that replaces OCR)

`ClaudeClient.extractReceipt(imageBase64)` sends the receipt image to Claude with a system prompt that pins the output schema:

```json
{
  "items": [
    {"name":"Organic Bananas","category":"produce","quantity":"1 bunch","shelfLifeDays":5,"confidence":0.9}
  ],
  "storeName": "Trader Joe's",
  "purchasedAtIso": "2026-04-16"
}
```

`ReceiptParser` strips any accidental ```` ``` ```` fences and decodes it into `ParsedReceipt`. `InventoryRepository.addParsedItems` stamps each row with `expiresAt = purchasedAt + shelfLifeDays` (falling back to `ShelfLife.defaultDaysFor(category)`).

## Recipe ranking

`RecipeEngine.recommend(inventory, now)` scores each recipe by

```
urgencyScore = Σ_ingredients_you_have  1 / max(1, daysLeft + 1)
```

then secondary-sorts by fewest missing ingredients. Starter recipes are hardcoded; move to `assets/recipes.json` or Spoonacular/Edamam when ready.

## What's stubbed vs. real

| Done | Stubbed |
|---|---|
| Room schema + CRUD | Camera capture (using PickVisualMedia instead) |
| Claude API call (image + JSON schema prompt) | Shopping-list generation button (placeholder) |
| Inventory list with consume/delete | Manual-override expiration date picker |
| Recipe ranking by urgency | Recipe DB beyond 4 starter recipes |
| 3-tab navigation | Hilt/DI (manual service locator for now) |

## Next up

- CameraX capture instead of gallery picker
- Date-picker dialog for manual expiry override
- `assets/recipes.json` loader; swap starter recipes for a real dataset
- Shopping-list screen driven off `RecipeMatch.missing`
- Wire up Gradle wrapper (`./gradlew wrapper` locally) — not checked in

## Team

Michael Li, Brian Hsu, Andy Chen, Hannah Chen, Jun Kim
