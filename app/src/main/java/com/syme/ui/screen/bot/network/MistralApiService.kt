// MistralApiClient.kt
package com.syme.ui.screen.bot.network

import com.syme.domain.model.ChatMessage
import com.syme.BuildConfig
import com.syme.domain.model.UserContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val MISTRAL_ENDPOINT = "https://api.mistral.ai/v1/chat/completions"
private const val MISTRAL_MODEL    = "mistral-large-latest"
private const val MAX_HISTORY      = 12
private const val MAX_TOKENS       = 1024
private const val TEMPERATURE      = 0.65

private val httpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
}

fun buildSystemPrompt(ctx: UserContext): String {
    val userSection = buildString {
        appendLine("## Contexte de l'utilisateur")
        ctx.userName?.let            { appendLine("- Nom : $it") }
        ctx.installationName?.let    { appendLine("- Installation : $it") }
        ctx.installationType?.let    { appendLine("- Type d'installation : $it") }
        ctx.totalEnergyWh?.let       { appendLine("- Énergie totale estimée : ${it} Wh/jour") }
        ctx.applianceCount?.let      { appendLine("- Nombre d'appareils : $it") }
        ctx.applianceSummary?.let    { appendLine("- Appareils principaux : $it") }
        ctx.lastConsumptionKwh?.let  { appendLine("- Dernière consommation : ${"%.1f".format(it)} kWh") }
        ctx.lastBillAmountXaf?.let   { appendLine("- Dernière facture : ${"%.0f".format(it)} FCFA") }
        ctx.tariffXafPerKwh?.let     { appendLine("- Tarif appliqué : ${"%.1f".format(it)} FCFA/kWh") }
        ctx.circuitCount?.let        { appendLine("- Circuits électriques : $it") }
        ctx.meterCount?.let          { appendLine("- Compteurs connectés : $it") }
    }

    return """
You are **SYME Bot**, an AI assistant integrated in the SYME electricity management app.
You are an expert in electricity, energy consumption, and optimization.

Always reply in the **same language as the user's message**. If the user writes in English, respond in English; if in French, respond in French.

$userSection

## Role
- Answer questions about electricity, installations, devices, bills, and energy consumption.
- Use the above user context to personalize your answers.
- Give practical and clear advice.

## Strict Scope
Tu réponds UNIQUEMENT aux questions liées à :
- L'électricité (circuits, lois, sécurité, normes)
- Les appareils et équipements électriques
- La consommation et les factures d'énergie
- L'installation électrique de l'utilisateur dans SYME
- Les énergies renouvelables (solaire, éolien)
- Les diagnostics et pannes électriques

Si une question est hors périmètre, réponds exactement :
"Je suis SYME Bot, spécialisé dans la gestion de ton installation électrique. Je ne peux pas répondre à cette question. Pose-moi une question sur l'électricité ou ta consommation !"

## Règles de réponse
- En français ou en anglais selon la langue de la question, sois clair et pédagogique.
- Cite les données réelles de l'utilisateur quand c'est pertinent.
- Pour les calculs, montre les étapes.
- Sois concis mais complet (max 5 paragraphes).
""".trimIndent()
}

suspend fun callMistralAPI(
    messages: List<ChatMessage>,
    userContext: UserContext = UserContext(),
    onError: (String) -> Unit
): String? = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.MISTRAL_API_KEY
    if (apiKey.isBlank()) {
        onError("Clé API manquante. Vérifiez local.properties.")
        return@withContext null
    }

    try {
        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", buildSystemPrompt(userContext))
            })
            messages
                .takeLast(MAX_HISTORY)
                .filter { !it.isLoading && !it.isError }
                .forEach { msg ->
                    put(JSONObject().apply {
                        put("role", if (msg.isUser) "user" else "assistant")
                        put("content", msg.content)
                    })
                }
        }

        val body = JSONObject().apply {
            put("model", MISTRAL_MODEL)
            put("messages", messagesArray)
            put("max_tokens", MAX_TOKENS)
            put("temperature", TEMPERATURE)
        }

        val request = Request.Builder()
            .url(MISTRAL_ENDPOINT)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (!response.isSuccessful || responseBody == null) {
            val detail = when (response.code) {
                401 -> "Clé API invalide ou expirée (401). Vérifiez BuildConfig.MISTRAL_API_KEY."
                429 -> "Quota dépassé (429). Réessayez dans quelques secondes."
                else -> "Erreur API ${response.code}"
            }
            onError(detail)
            return@withContext null
        }

        JSONObject(responseBody)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

    } catch (e: Exception) {
        onError("Erreur réseau : ${e.message}")
        null
    }
}