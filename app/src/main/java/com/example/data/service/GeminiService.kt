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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateAiTeacherResponse(
        userPrompt: String,
        retrievedKnowledge: List<KnowledgeEntity>,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val trimmedPrompt = userPrompt.trim()
        val lowerPrompt = trimmedPrompt.lowercase()

        // 1. Strict identity check ("आप कौन हैं?", "Who are you?", etc.)
        if (isIdentityQuestion(lowerPrompt)) {
            return@withContext "मैं एक AI टीचर हूँ, मुझे SP ने बनाया है। मैं आपकी पढ़ाई और सभी विषयों के डाउट हल करने के लिए हमेशा तत्पर हूँ।"
        }

        // 2. Strict creator identity check
        if (isCreatorQuestion(lowerPrompt)) {
            return@withContext "मुझे SP ने बनाया है, और यह मिथिला अकादमी (Mithila Academy Darbhanga) द्वारा निर्मित किया गया है।"
        }

        // 3. Greeting check
        if (isGreetingQuestion(lowerPrompt)) {
            return@withContext "मैं मिथिला अकादमी द्वारा निर्मित किया गया एक AI टीचर हूँ और मुझे SP ने बनाया है। आपका क्या सवाल है? आप मुझे बताएं, मैं उसका उत्तर अभी देता हूँ।"
        }

        // 4. Try calling Gemini API if API key is provided
        val apiKey = getApiKey()
        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val apiResponse = callGeminiApi(trimmedPrompt, apiKey, retrievedKnowledge, conversationHistory)
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
        conversationHistory: List<Pair<String, String>>
    ): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val knowledgeContext = if (retrievedKnowledge.isNotEmpty()) {
            val knowledgeText = retrievedKnowledge.joinToString("\n\n") { item ->
                "[Topic: ${item.title} (${item.subject})]:\n${item.content}"
            }
            "\n\n[Mithila Academy & SP Knowledge Base]:\n$knowledgeText"
        } else {
            ""
        }

        val systemInstructionText = """
            आप मिथिला अकादमी दरभंगा के लिए एक AI टीचर हैं।

            अनिवार्य पहचान और निर्माता नियम:
            - आपका नाम: AI टीचर (AI Teacher)
            - आपके निर्माता: SP (SP Yadav / SP Sir)
            - संस्थान: मिथिला अकादमी दरभंगा (Mithila Academy Darbhanga)
            - कभी भी "ESPA AI", "S-P-A-I" या किसी अन्य भ्रामक संक्षिप्त नाम का प्रयोग न करें।
            - जब भी कोई आपकी पहचान पूछे, तो विनम्रता से स्पष्ट हिंदी में कहें: "मैं एक AI टीचर हूँ, मुझे SP ने बनाया है।"
            - जब भी कोई आपसे शुरुआत में अभिवादन करे या परिचय मांगे, तो कहें: "मैं मिथिला अकादमी द्वारा निर्मित किया गया एक AI टीचर हूँ और मुझे SP ने बनाया है। आपका क्या सवाल है? आप मुझे बताएं, मैं उसका उत्तर अभी देता हूँ।"
            - यदि कोई पूछे कि आपको किसने बनाया: तो कहें: "मुझे SP ने बनाया है, और यह मिथिला अकादमी (Mithila Academy Darbhanga) द्वारा निर्मित किया गया है।"

            अनिवार्य भाषा और शिक्षण शैली (STRICT HINDI & STUDENT-FRIENDLY):
            1. आपको हमेशा केवल और केवल स्पष्ट, शुद्ध, मधुर और सरल हिंदी (देवनागरी लिपि) में उत्तर देना है।
            2. किसी भी अंग्रेजी लहजे या केवल अंग्रेजी भाषा में उत्तर न दें।
            3. प्रत्येक विषय (भौतिक विज्ञान, रसायन विज्ञान, जीव विज्ञान, गणित, सामान्य ज्ञान, बिहार विशेष, रेलवे एवं SSC) को चरणबद्ध (Step-by-Step), बेहद आसान और उदाहरणों के साथ समझाएं।
            4. जटिल से जटिल सूत्र व कॉन्सेप्ट को ऐसे समझाएं कि कक्षा 9 से 12वीं तथा प्रतियोगी परीक्षा की तैयारी कर रहा कोई भी छात्र आसानी से समझ सके।
            5. आवाज (TTS) के लिए अनुकूल रखें: अनावश्यक चिन्ह, अजीब कोड या अनचाहे सिंबल न लगाएं ताकि ऑडियो सुनने में बहुत स्वाभाविक और मधुर लगे।
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

        // Contents array
        val contentsArray = JSONArray()

        // Add recent conversation context
        conversationHistory.takeLast(4).forEach { (role, text) ->
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
            put("maxOutputTokens", 1024)
        }
        rootJson.put("generationConfig", generationConfig)

        val body = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e("GeminiService", "API response error code ${response.code}: $responseBody")
            return ""
        }

        val resJson = JSONObject(responseBody)
        val candidates = resJson.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "").trim()
            }
        }
        return ""
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
