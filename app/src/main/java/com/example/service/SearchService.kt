package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.AppDatabase
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class SearchService(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun ask(
        query: String,
        additionalContext: String? = null,
        attachedFileBase64: String? = null,
        attachedFileMimeType: String? = null
    ): String = withContext(Dispatchers.IO) {
        _searching.value = true
        try {
            // 1. Search locally first
            val cleanQuery = "%${query.trim()}%"
            val matchedDocs = db.documentDao().searchDocuments(cleanQuery)
            
            val localResults = StringBuilder()
            if (matchedDocs.isNotEmpty()) {
                matchedDocs.forEach { doc ->
                    localResults.append("🎖️ ${doc.title}\n${doc.content}\n\n")
                }
            }

            if (!additionalContext.isNullOrEmpty()) {
                localResults.append("🎖️ معلومات إضافية من الملف المرفق:\n$additionalContext\n\n")
            }

            // Also search programs
            val matchedPrograms = db.programDao().getAllPrograms()
            val programMatches = StringBuilder()
            
            // Loop through all saved programs is fast and easy
            // We read database flow using a one-shot read or standard Room query
            val allProgramsEntities = db.programDao().getAllPrograms()
            // We can search through the list
            // (Note: Flow can be collected or we can query directly, list search is very simple)
            
            val apiKey = try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            val hasGemini = apiKey.isNotEmpty() && !apiKey.contains("placeholder") && !apiKey.contains("MY_GEMINI")

            if (localResults.isEmpty() && attachedFileBase64.isNullOrEmpty()) {
                if (hasGemini) {
                    // Try to generate a smart general response about the college contextually
                    val response = callGeminiApi(query, apiKey, "أجب بأسلوب عسكري ووقور بصفتك المساعد الذكي لكلية الطب والعلوم الصحية العسكرية. ليس لدينا نصوص محلية مباشرة عن هذا السؤال، ولكن أجب بما تملك من معرفة عامة عن الكلية والمعهد الطبي عسكرياً في اليمن.", null, null)
                    return@withContext response
                } else {
                    return@withContext "لم يتم العثور على معلومات كافية في مراجع الكلية المحلية."
                }
            }

            if (hasGemini) {
                // Synthesize the found database items using Gemini for a highly polished smart answer!
                val systemPrompt = """
                    أنت المساعد الذكي 'مرشد الشهيد الدكتور زيد طاووس' لكلية الطب والعلوم الصحية العسكرية والمعهد الطبي العسكري.
                    قم بصياغة إجابة ملخصة ومترابطة ودقيقة باللغة العربية بناءً على المراجع المستخرجة حصراً أدناه.
                    أجب بنبرة عسكرية، وقورة، ومنظمة مع استخدام الرموز العسكرية (🎖️) والترقيم المناسب.
                    عند وجود مستند أو صورة مرفقة بالمحاورة، فسر محتواها التكتيكي أو الطبي بدقة فائقة واربطه بالمناهج والمقررات الميدانية.
                    
                    المراجع المستخرجة:
                    $localResults
                """.trimIndent()
                
                val synthesized = callGeminiApi(query, apiKey, systemPrompt, attachedFileBase64, attachedFileMimeType)
                if (synthesized.startsWith("خطأ") || synthesized.isEmpty()) {
                    return@withContext localResults.toString().trim()
                }
                return@withContext synthesized
            } else {
                return@withContext localResults.toString().trim()
            }
        } catch (e: Exception) {
            Log.e("SearchService", "Error during search: ${e.localizedMessage}")
            return@withContext "🎖️ المراجع المحلية للكلية:\nحدث خطأ أثناء معالجة السؤال الذكي: ${e.localizedMessage}"
        } finally {
            _searching.value = false
        }
    }

    suspend fun callGeminiApi(
        prompt: String,
        apiKey: String,
        systemInstruction: String,
        attachedFileBase64: String? = null,
        attachedFileMimeType: String? = null
    ): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        try {
            val jsonObject = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            if (!attachedFileBase64.isNullOrEmpty() && !attachedFileMimeType.isNullOrEmpty()) {
                                put(JSONObject().apply {
                                    put("inlineData", JSONObject().apply {
                                        put("mimeType", attachedFileMimeType)
                                        put("data", attachedFileBase64)
                                    })
                                })
                            }
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e("SearchService", "Gemini HTTP error ${response.code}: $errBody")
                    return@withContext ""
                }

                val responseBodyStr = response.body?.string() ?: ""
                val resObj = JSONObject(responseBodyStr)
                val candidates = resObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text") ?: ""
                        }
                    }
                }
                return@withContext ""
            }
        } catch (e: Exception) {
            Log.e("SearchService", "Gemini Exception: ${e.localizedMessage}")
            return@withContext ""
        }
    }
}
