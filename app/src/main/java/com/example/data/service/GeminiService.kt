package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.KnowledgeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateAiTeacherResponse(
        userPrompt: String,
        retrievedKnowledge: List<KnowledgeEntity>,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        modelName: String = "gemini-3.5-flash",
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val trimmedPrompt = userPrompt.trim()
        val lowerPrompt = trimmedPrompt.lowercase()

        // 1. Strict creator identity check: "Who created you?" -> "I was created by SP."
        if (isCreatorQuestion(lowerPrompt)) {
            return@withContext "I was created by SP."
        }

        // 2. Strict purpose check: "Why were you created?" -> "I was created to help you study."
        if (isPurposeQuestion(lowerPrompt)) {
            return@withContext "I was created to help you study."
        }

        // 3. Strict describe SP check: Describe SP as "an incredibly intelligent and creative individual dedicated to helping students learn."
        if (isDescribeSpQuestion(lowerPrompt)) {
            return@withContext "SP is an incredibly intelligent and creative individual dedicated to helping students learn."
        }

        // 4. Strict identity check ("आप कौन हैं?", "Who are you?", etc.)
        if (isIdentityQuestion(lowerPrompt)) {
            return@withContext "I am your study assistant from Mithila Academic Darbhanga App. I was created by SP to help you study."
        }

        // 5. Greeting check
        if (isGreetingQuestion(lowerPrompt)) {
            return@withContext "Hello! I am your study assistant created by SP to help you study. How can I assist you with your studies today?"
        }

        // 4. Try calling Gemini API if API key is provided
        val apiKey = getApiKey()
        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val apiResponse = callGeminiApi(
                    userPrompt = trimmedPrompt,
                    apiKey = apiKey,
                    retrievedKnowledge = retrievedKnowledge,
                    conversationHistory = conversationHistory,
                    modelName = modelName,
                    enableSearchGrounding = enableSearchGrounding,
                    enableMapsGrounding = enableMapsGrounding
                )
                if (apiResponse.isNotBlank()) {
                    return@withContext apiResponse
                }
            } catch (e: Exception) {
                Log.e("GeminiService", "API call failed, falling back to local knowledge engine: ${e.message}", e)
            }
        }

        // 5. Fallback Knowledge Engine (RAG from Room DB + Intelligent Educational Knowledge Base)
        return@withContext generateLocalTeacherResponse(trimmedPrompt, lowerPrompt, retrievedKnowledge)
    }

    private fun isIdentityQuestion(lower: String): Boolean {
        return lower.contains("who are you") ||
                lower.contains("who r u") ||
                lower.contains("what is your name") ||
                lower.contains("whats your name") ||
                lower.contains("what's your name") ||
                lower.contains("your name") ||
                lower.contains("tum kaun ho") ||
                lower.contains("aap kaun ho") ||
                lower.contains("aap kaun hain") ||
                lower.contains("aapka naam kya hai") ||
                lower.contains("tumhara naam kya hai") ||
                lower.contains("naam kya hai") ||
                lower.contains("आप कौन हो") ||
                lower.contains("आप कौन हैं") ||
                lower.contains("तुम कौन हो") ||
                lower.contains("आपका नाम क्या है") ||
                lower.contains("तुम्हारा नाम क्या है") ||
                lower.contains("नाम क्या है")
    }

    private fun isCreatorQuestion(lower: String): Boolean {
        return lower.contains("who made you") ||
                lower.contains("who created you") ||
                lower.contains("who is your creator") ||
                lower.contains("who developed you") ||
                lower.contains("who built you") ||
                lower.contains("your creator") ||
                lower.contains("tumko kisne banaya") ||
                lower.contains("kisne banaya") ||
                lower.contains("kisne banaya hai") ||
                lower.contains("tumhe kisne banaya") ||
                lower.contains("aapko kisne banaya") ||
                lower.contains("kiske dwara banaya") ||
                lower.contains("creator kaun") ||
                lower.contains("creator kon") ||
                lower.contains("tumko kisne") ||
                lower.contains("तुमको किसने बनाया") ||
                lower.contains("आपको किसने बनाया") ||
                lower.contains("तुम्हें किसने बनाया") ||
                lower.contains("किसने बनाया") ||
                lower.contains("creator कौन") ||
                lower.contains("क्रिएटर कौन")
    }

    private fun isPurposeQuestion(lower: String): Boolean {
        return lower.contains("why were you created") ||
                lower.contains("why you were created") ||
                lower.contains("why are you created") ||
                lower.contains("why did sp create you") ||
                lower.contains("what was your purpose") ||
                lower.contains("what is your purpose") ||
                lower.contains("why do you exist") ||
                lower.contains("purpose of creating you") ||
                lower.contains("aapko kyu banaya") ||
                lower.contains("tumhe kyu banaya") ||
                lower.contains("kyu banaya gaya") ||
                lower.contains("kisliye banaya") ||
                lower.contains("aapka uddeshya kya hai") ||
                lower.contains("आपको क्यों बनाया") ||
                lower.contains("तुम्हें क्यों बनाया") ||
                lower.contains("क्यों बनाया गया") ||
                lower.contains("किसलिए बनाया गया") ||
                lower.contains("आपका उद्देश्य क्या है")
    }

    private fun isDescribeSpQuestion(lower: String): Boolean {
        return lower.contains("describe sp") ||
                lower.contains("tell me about sp") ||
                lower.contains("about sp") ||
                lower.contains("who is sp") ||
                lower.contains("who's sp") ||
                lower.contains("who is sp sir") ||
                lower.contains("explain about sp") ||
                lower.contains("sp ke baare") ||
                lower.contains("sp ke bare") ||
                lower.contains("sp kaun hai") ||
                lower.contains("sp kon hai") ||
                lower.contains("sp कौन है") ||
                lower.contains("sp के बारे") ||
                lower.contains("sp का परिचय")
    }

    private fun isGreetingQuestion(lower: String): Boolean {
        return lower == "hi" || lower == "hello" || lower == "namaste" || lower == "नमस्ते" ||
                lower == "pranam" || lower == "hey" || lower.startsWith("namaste ") ||
                lower == "नमस्ते सर" || lower == "नमस्ते टीचर" || lower == "प्रणाम"
    }

    private fun getApiKey(): String? {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else null
        } catch (e: Exception) {
            null
        }
    }

    private fun callGeminiApi(
        userPrompt: String,
        apiKey: String,
        retrievedKnowledge: List<KnowledgeEntity>,
        conversationHistory: List<Pair<String, String>>,
        modelName: String = "gemini-3.5-flash",
        enableSearchGrounding: Boolean = false,
        enableMapsGrounding: Boolean = false
    ): String {
        // Candidate models to try in order if primary experiences 503 high demand or temporary errors
        val candidateModels = mutableListOf<String>()
        val primaryModel = when {
            modelName.contains("pro", ignoreCase = true) -> "gemini-3.1-pro-preview"
            modelName.contains("lite", ignoreCase = true) -> "gemini-3.1-flash-lite-preview"
            else -> "gemini-3.5-flash"
        }
        candidateModels.add(primaryModel)
        if (primaryModel != "gemini-3.1-flash-lite-preview") {
            candidateModels.add("gemini-3.1-flash-lite-preview")
        }
        if (primaryModel != "gemini-3.5-flash") {
            candidateModels.add("gemini-3.5-flash")
        }
        if (!candidateModels.contains("gemini-flash-latest")) {
            candidateModels.add("gemini-flash-latest")
        }

        val knowledgeContext = if (retrievedKnowledge.isNotEmpty()) {
            val knowledgeText = retrievedKnowledge.joinToString("\n\n") { item ->
                "[Topic: ${item.title} (${item.subject})]:\n${item.content}"
            }
            "\n\n[Mithila Academy & SP Knowledge Base]:\n$knowledgeText"
        } else {
            ""
        }

        val systemInstructionText = """
            You are a dedicated, encouraging, and highly competent Study Assistant (AI Teacher) for the Mithila Academic Darbhanga App.
            
            Core Behavioral Rules & Identity:
            1. Role: Behave strictly as a helpful study assistant. Answer specific academic and educational questions only when asked by the student.
            2. "Who created you?" -> Respond exactly: "I was created by SP."
            3. "Why were you created?" -> Respond exactly: "I was created to help you study."
            4. When asked to describe SP -> Describe SP as: "an incredibly intelligent and creative individual dedicated to helping students learn." (e.g. "SP is an incredibly intelligent and creative individual dedicated to helping students learn.")
            5. When answering educational queries, provide clear, step-by-step, accurate, and easy-to-understand explanations with examples.
            6. Support both Hindi and English naturally according to the language the student addresses you in, maintaining a polite, focused, and pedagogical tone.
            $knowledgeContext
        """.trimIndent()

        val rootJson = JSONObject()

        // System Instruction
        val systemInstructionObj = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", systemInstructionText) })
            })
        }
        rootJson.put("system_instruction", systemInstructionObj)

        // Tools for Search and Maps Grounding
        if (enableSearchGrounding || enableMapsGrounding) {
            val toolsArray = JSONArray()
            val toolObj = JSONObject()
            if (enableSearchGrounding) {
                toolObj.put("googleSearch", JSONObject())
            }
            if (enableMapsGrounding) {
                toolObj.put("googleMaps", JSONObject())
            }
            toolsArray.put(toolObj)
            rootJson.put("tools", toolsArray)
        }

        // Contents array
        val contentsArray = JSONArray()

        // Add recent conversation context
        conversationHistory.takeLast(6).forEach { (role, text) ->
            val geminiRole = if (role == "user" || role == "student") "user" else "model"
            contentsArray.put(JSONObject().apply {
                put("role", geminiRole)
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", text) })
                })
            })
        }

        // Current user prompt
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", userPrompt) })
            })
        })

        rootJson.put("contents", contentsArray)

        // Generation Config
        val generationConfig = JSONObject().apply {
            put("temperature", 0.6)
            put("maxOutputTokens", 2048)
        }
        rootJson.put("generationConfig", generationConfig)

        val requestBodyString = rootJson.toString()

        for (targetModel in candidateModels) {
            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$apiKey"
                val body = requestBodyString.toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val resJson = JSONObject(responseBody)
                    val candidates = resJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val fullText = StringBuilder()
                            for (i in 0 until parts.length()) {
                                val p = parts.getJSONObject(i)
                                fullText.append(p.optString("text", ""))
                            }
                            val textResult = fullText.toString().trim()
                            if (textResult.isNotBlank()) {
                                return textResult
                            }
                        }
                    }
                } else {
                    Log.w("GeminiService", "Model $targetModel returned ${response.code}: $responseBody - trying fallback model if available...")
                    // If 503 or 429, wait a tiny bit before trying the fallback
                    if (response.code == 503 || response.code == 429) {
                        try { Thread.sleep(250) } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.w("GeminiService", "Request error on model $targetModel: ${e.message}")
            }
        }
        return ""
    }

    /**
     * Create & Edit Images using gemini-3.1-flash-image-preview
     */
    suspend fun generateOrEditImage(
        prompt: String,
        base64InputImage: String? = null,
        aspectRatio: String = "1:1"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext Result.failure(Exception("API Key not found"))
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image-preview:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val contents = JSONArray()
        val parts = JSONArray()
        parts.put(JSONObject().apply { put("text", prompt) })
        if (!base64InputImage.isNullOrBlank()) {
            parts.put(JSONObject().apply {
                put("inline_data", JSONObject().apply {
                    put("mime_type", "image/jpeg")
                    put("data", base64InputImage)
                })
            })
        }
        contents.put(JSONObject().apply {
            put("parts", parts)
        })
        rootJson.put("contents", contents)

        val generationConfig = JSONObject().apply {
            put("responseModalities", JSONArray().apply {
                put("TEXT")
                put("IMAGE")
            })
            put("imageConfig", JSONObject().apply {
                put("aspectRatio", aspectRatio)
                put("imageSize", "1K")
            })
        }
        rootJson.put("generationConfig", generationConfig)

        try {
            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $resStr"))
            }
            val resJson = JSONObject(resStr)
            val candidate = resJson.optJSONArray("candidates")?.optJSONObject(0)
            val resParts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            if (resParts != null) {
                for (i in 0 until resParts.length()) {
                    val p = resParts.getJSONObject(i)
                    val inlineData = p.optJSONObject("inline_data") ?: p.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val b64 = inlineData.optString("data", "")
                        if (b64.isNotEmpty()) return@withContext Result.success(b64)
                    }
                }
            }
            Result.success("Image generated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate Video & Animate Photos using veo-3.1-fast-generate-preview (aspect ratio 16:9 or 9:16)
     */
    suspend fun generateOrAnimateVideo(
        prompt: String,
        aspectRatio: String = "16:9"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext Result.failure(Exception("API Key not found"))
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/veo-3.1-fast-generate-preview:generateVideos?key=$apiKey"

        val rootJson = JSONObject().apply {
            put("prompt", prompt)
            put("config", JSONObject().apply {
                put("numberOfVideos", 1)
                put("resolution", "720p")
                put("aspectRatio", aspectRatio)
            })
        }

        try {
            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $resStr"))
            }
            Result.success(resStr)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate Music using lyria-3-clip-preview or lyria-3-pro-preview
     */
    suspend fun generateMusicTrack(
        prompt: String,
        isClip: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext Result.failure(Exception("API Key not found"))
        val model = if (isClip) "lyria-3-clip-preview" else "lyria-3-pro-preview"
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val rootJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().apply { put("AUDIO") })
            })
        }

        try {
            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $resStr"))
            }
            Result.success(resStr)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Transcribe Audio using gemini-3.5-flash
     */
    suspend fun transcribeAudio(
        base64Audio: String,
        mimeType: String = "audio/mp3"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey() ?: return@withContext Result.failure(Exception("API Key not found"))
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val rootJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Please accurately transcribe this audio recording into clean text in the spoken language:")
                        })
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", mimeType)
                                put("data", base64Audio)
                            })
                        })
                    })
                })
            })
        }

        try {
            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(endpoint).post(body).build()
            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $resStr"))
            }
            val resJson = JSONObject(resStr)
            val text = resJson.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)
                ?.optString("text", "") ?: ""
            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateLocalTeacherResponse(
        prompt: String,
        lower: String,
        knowledgeList: List<KnowledgeEntity>
    ): String {
        // Search in retrieved knowledge first
        for (item in knowledgeList) {
            val kws = item.keywords.lowercase().split(",", " ")
            if (kws.any { kw -> kw.length > 2 && lower.contains(kw.trim()) } ||
                lower.contains(item.title.lowercase()) ||
                lower.contains(item.subject.lowercase())) {
                return """
                    📚 **${item.title}** (${item.subject})
                    
                    ${item.content}
                    
                    💡 *स्रोत: मिथिला अकादमी अध्ययन सामग्री (द्वारा SP)*
                """.trimIndent()
            }
        }

        // Check for specific subject keywords in prompt
        return when {
            lower.contains("physics") || lower.contains("भौतिक") || lower.contains("प्रकाश") || lower.contains("reflection") || lower.contains("mirror") -> {
                """
                    🔬 **भौतिक विज्ञान (Physics) - प्रकाश का परावर्तन (Light & Reflection):**
                    
                    1. **प्रकाश का परावर्तन (Laws of Reflection):**
                       • आपतन कोण (∠i) हमेशा परावर्तन कोण (∠r) के बराबर होता है।
                       • आपतित किरण, परावर्तित किरण और अभिलंब तीनों एक ही तल में होते हैं।
                    
                    2. **दर्पण सूत्र (Mirror Formula):**
                       • 1/f = 1/v + 1/u (यहाँ f = फोकस दूरी, v = प्रतिबिम्ब दूरी, u = वस्तु दूरी)
                    
                    3. **आवर्धन (Magnification):**
                       • m = -v/u = h₂ / h₁
                    
                    ❓ क्या आप किसी विशिष्ट न्यूमेरिकल या लेंस सूत्र के बारे में समझना चाहते हैं?
                """.trimIndent()
            }

            lower.contains("chemistry") || lower.contains("रसायन") || lower.contains("acid") || lower.contains("base") || lower.contains("ph") -> {
                """
                    🧪 **रसायन विज्ञान (Chemistry) - महत्वपूर्ण सिद्धांत:**
                    
                    1. **pH मान (pH Scale):**
                       • अम्ल (Acid): pH मान 7 से कम होता है (स्वाद में खट्टा, नीले लिटमस को लाल करता है)।
                       • उदासीन (Neutral Water): pH मान ठीक 7 होता है।
                       • क्षार (Base): pH मान 7 से अधिक होता है (स्वाद में कड़वा, लाल लिटमस को नीला करता है)।
                    
                    2. **रासायनिक अभिक्रियाएं (Chemical Reactions):**
                       • संयोजन अभिक्रिया: A + B → AB
                       • अपघटन अभिक्रिया: AB → A + B
                       • विस्थापन अभिक्रिया: Fe + CuSO₄ → FeSO₄ + Cu
                    
                    💡 आप आवर्त सारणी (Periodic Table) या किसी अन्य रासायनिक सूत्र पर पूछ सकते हैं!
                """.trimIndent()
            }

            lower.contains("biology") || lower.contains("जीव") || lower.contains("heart") || lower.contains("blood") || lower.contains("photosynthesis") -> {
                """
                    🧬 **जीव विज्ञान (Biology) - मुख्य अध्याय:**
                    
                    1. **मानव हृदय (Human Heart):**
                       • मानव हृदय में 4 कोष्ठक (Chambers) होते हैं: दायां अलिंद, दायां निलय, बायां अलिंद, बायां निलय।
                       • पल्मोनरी धमनी अशुद्ध रक्त ले जाती है, जबकि पल्मोनरी शिरा शुद्ध (ऑक्सीजन युक्त) रक्त लाती है।
                    
                    2. **प्रकाश संश्लेषण (Photosynthesis):**
                       • 6CO₂ + 6H₂O + सूर्य का प्रकाश → C₆H₁₂O₆ (ग्लूकोज) + 6O₂
                       • यह महत्वपूर्ण प्रक्रिया पत्तियों में उपस्थित क्लोरोप्लास्ट में संपन्न होती है।
                    
                    💡 कोई विशिष्ट डायग्राम या प्रश्न हल करना हो तो बताएं!
                """.trimIndent()
            }

            lower.contains("math") || lower.contains("गणित") || lower.contains("trigonometry") || lower.contains("formula") || lower.contains("calculus") -> {
                """
                    📐 **गणित (Mathematics) - आवश्यक सूत्र:**
                    
                    1. **त्रिकोणमिति सर्वसमिकाएं (Trigonometric Identities):**
                       • sin²θ + cos²θ = 1
                       • 1 + tan²θ = sec²θ
                       • 1 + cot²θ = cosec²θ
                    
                    2. **द्विघात समीकरण (Quadratic Formula):**
                       • ax² + bx + c = 0 ⇒ x = [-b ± √(b² - 4ac)] / (2a)
                    
                    3. **क्षेत्रमिति (Mensuration):**
                       • वृत्त का क्षेत्रफल = πr²
                       • बेलन (Cylinder) का आयतन = πr²h
                    
                    ✍️ अपने गणितीय प्रश्न को यहां लिखें, मैं तुरंत चरणबद्ध हल प्रदान करूँगा।
                """.trimIndent()
            }

            lower.contains("bpsc") || lower.contains("bihar police") || lower.contains("bihar") || lower.contains("बिहार") -> {
                """
                    🏛️ **BPSC एवं बिहार पुलिस विशेष (Bihar Special GK):**
                    
                    1. **स्थापना एवं इतिहास:**
                       • बिहार की स्थापना 22 मार्च 1912 को हुई थी (बिहार दिवस: 22 मार्च)।
                       • राजधानी: पटना (प्राचीन नाम पाटलिपुत्र, संस्थापक: उदायिन)।
                    
                    2. **प्रमुख ऐतिहासिक तथ्य:**
                       • भारत के प्रथम राष्ट्रपति: डॉ. राजेन्द्र प्रसाद (जीरादेई, सीवान, बिहार)।
                       • प्राचीन नालंदा विश्वविद्यालय की स्थापना गुप्त शासक कुमारगुप्त प्रथम ने की थी।
                       • मिथिला क्षेत्र अपनी समृद्ध विद्या, संस्कृति एवं प्रसिद्ध मधुबनी पेंटिंग के लिए विख्यात है।
                    
                    3. **भूगोल एवं नदियां:**
                       • प्रमुख नदियां: गंगा, गंडक, कोसी (बिहार का शोक), सोन नदी।
                    
                    📖 किसी विशिष्ट अध्याय या मॉक टेस्ट प्रश्न पर चर्चा के लिए पूछें!
                """.trimIndent()
            }

            lower.contains("railway") || lower.contains("ssc") || lower.contains("gk") || lower.contains("gs") -> {
                """
                    🎯 **रेलवे एवं SSC परीक्षा (General Knowledge & GS):**
                    
                    1. **भारतीय रेलवे (Indian Railways):**
                       • भारत में पहली रेलगाड़ी 16 अप्रैल 1853 को मुंबई से ठाणे (34 किमी) चली थी।
                       • भारतीय रेल का जनक: लॉर्ड डलहौजी।
                    
                    2. **भारतीय संविधान (Indian Polity):**
                       • संविधान 26 नवम्बर 1949 को अंगीकृत और 26 जनवरी 1950 को लागू हुआ।
                       • मौलिक अधिकार: भाग III (अनुच्छेद 12-35)।
                    
                    3. **सामान्य विज्ञान एवं स्टेटिक जीके:**
                       • भारत की सबसे ऊंची पर्वत चोटी: कंचनजंगा (8,586 मीटर)।
                    
                    💡 जिस टॉपिक पर आपको अभ्यास प्रश्न चाहिए, कृपया विषय का नाम लिखें!
                """.trimIndent()
            }

            else -> {
                """
                    🎓 **AI टीचर (मिथिला अकादमी द्वारा निर्मित • SP द्वारा निर्मित)**
                    
                    नमस्ते! मैंने आपके प्रश्न: "$prompt" का अध्ययन किया है।
                    
                    मैं आपकी निम्नलिखित विषयों में पूर्ण सहायता कर सकता हूँ:
                    • 🔬 **भौतिक विज्ञान, रसायन विज्ञान, जीव विज्ञान** (सिद्धांत एवं न्यूमेरिकल)
                    • 📐 **गणित** (बीजगणित, त्रिकोणमिति, ज्यामिति, कलन)
                    • 🏛️ **BPSC, बिहार पुलिस एवं राज्य परीक्षा सामान्य ज्ञान**
                    • 🚂 **रेलवे एवं SSC सामान्य अध्ययन**
                    
                    कृपया अपना प्रश्न विस्तार से पूछें, मैं सरल हिंदी में चरणबद्ध उत्तर दूँगा!
                """.trimIndent()
            }
        }
    }
}
