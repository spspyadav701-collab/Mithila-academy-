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
            return@withContext "मैं SPA AI Teacher हूँ। मैं आपकी पढ़ाई और सीखने में मदद करने के लिए यहाँ हूँ।"
        }

        // 2. Strict creator identity check
        if (isCreatorQuestion(lowerPrompt)) {
            return@withContext if (isEnglishCreatorQuestion(lowerPrompt)) {
                "I was created by SP and developed by Mithila Academy."
            } else {
                "मुझे SP ने बनाया है, और यह Mithila Academy द्वारा निर्मित किया गया है।"
            }
        }

        // 3. Greeting check
        if (isGreetingQuestion(lowerPrompt)) {
            return@withContext "नमस्ते, मैं SPA AI Teacher हूँ। आपकी क्या मदद कर सकता हूँ?"
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

    private fun isEnglishCreatorQuestion(lower: String): Boolean {
        return lower.contains("who made") ||
                lower.contains("who created") ||
                lower.contains("who is your creator") ||
                lower.contains("who developed") ||
                lower.contains("who built") ||
                (lower.contains("creator") && !lower.contains("kaun") && !lower.contains("kon") && !lower.contains("कौन"))
    }

    private fun isGreetingQuestion(lower: String): Boolean {
        return lower == "hi" || lower == "hello" || lower == "namaste" || lower == "नमस्ते" ||
                lower == "pranam" || lower == "hey" || lower == "spa" || lower.startsWith("namaste ") ||
                lower == "नमस्ते सर" || lower == "नमस्ते टीचर"
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
            You are SPA AI Teacher. Your name is SPA AI Teacher.
            
            CREATOR INFORMATION & IDENTITY RULES:
            - Creator: SP
            - Developed/produced by: Mithila Academy
            - Never claim that Zoya created you.
            - Never identify Zoya as your creator or your name under any circumstances.
            - Never invent another person's name as your creator.
            
            WHEN ASKED ABOUT CREATOR / WHO CREATED YOU:
            - If the user asks in Hindi/Hinglish (e.g. "तुमको किसने बनाया?", "आपको किसने बनाया?", "तुम्हें किसने बनाया है?", "किसने बनाया", "आपके creator कौन हैं?"):
              Respond: "मुझे SP ने बनाया है, और यह Mithila Academy द्वारा निर्मित किया गया है।"
            - If the user asks in English (e.g. "Who created you?", "Who made you?", "Who developed you?", "Who is your creator?"):
              Respond: "I was created by SP and developed by Mithila Academy."
            
            CRITICAL IDENTITY & GREETING RULES:
            1. If the user asks your name or identity (e.g. "आप कौन हैं?", "आप कौन हो?", "Who are you?", "तुम कौन हो?", "What is your name?", "आपका नाम क्या है?"):
               Respond: "मैं SPA AI Teacher हूँ। मैं आपकी पढ़ाई और सीखने में मदद करने के लिए यहाँ हूँ।"
            2. If the user greets you (e.g. "नमस्ते", "Hello", "Hi"):
               Respond: "नमस्ते, मैं SPA AI Teacher हूँ। आपकी क्या मदद कर सकता हूँ?"
            
            Core Educational Directives:
            - You are an expert teacher for Mithila Academy.
            - Teach and solve student doubts in Biology, Physics, Chemistry, Mathematics, General Knowledge (GK), and competitive exams such as Railway, SSC, BPSC, Bihar Police, and state board exams.
            - Provide clear, structured, encouraging, and step-by-step explanations in Hindi/English/Hinglish as preferred by the student. Use formulas, bullet points, and practical examples.
            - Utilize the provided Mithila Academy knowledge context when available.
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
            put("temperature", 0.7)
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
                    
                    💡 *स्रोत: मिथिला एकेडमी अध्ययन सामग्री (द्वारा SP)*
                """.trimIndent()
            }
        }

        // Check for specific subject keywords in prompt
        return when {
            lower.contains("physics") || lower.contains("भौतिक") || lower.contains("प्रकाश") || lower.contains("reflection") || lower.contains("mirror") -> {
                """
                    🔬 **Physics (भौतिक विज्ञान) - Light & Reflection:**
                    
                    1. **प्रकाश का परावर्तन (Laws of Reflection):**
                       • आपतन कोण (∠i) = परावर्तन कोण (∠r).
                       • आपतित किरण, परावर्तित किरण और अभिलंब एक ही तल में होते हैं।
                    
                    2. **दर्पण सूत्र (Mirror Formula):**
                       • 1/f = 1/v + 1/u (जहाँ f = फोकस दूरी, v = प्रतिबिम्ब दूरी, u = वस्तु दूरी)
                    
                    3. **आवर्धन (Magnification):**
                       • m = -v/u = h₂ / h₁
                    
                    ❓ क्या आप किसी विशिष्ट न्यूमेरिकल या लेंस सूत्र के बारे में जानना चाहते हैं?
                """.trimIndent()
            }

            lower.contains("chemistry") || lower.contains("रसायन") || lower.contains("acid") || lower.contains("base") || lower.contains("ph") -> {
                """
                    🧪 **Chemistry (रसायन विज्ञान) - Key Concepts:**
                    
                    1. **pH मान (pH Scale):**
                       • अम्ल (Acid): pH < 7 (स्वाद में खट्टा, नीले लिटमस को लाल करता है)
                       • उदासीन (Neutral Water): pH = 7
                       • क्षार (Base): pH > 7 (स्वाद में कड़वा, लाल लिटमस को नीला करता है)
                    
                    2. **रासायनिक अभिक्रियाएं (Reactions):**
                       • संयोजन (Combination): A + B → AB
                       • अपघटन (Decomposition): AB → A + B
                       • विस्थापन (Displacement): Fe + CuSO₄ → FeSO₄ + Cu
                    
                    💡 आप आवर्त सारणी (Periodic Table) या किसी अन्य रासायनिक सूत्र पर पूछ सकते हैं!
                """.trimIndent()
            }

            lower.contains("biology") || lower.contains("जीव") || lower.contains("heart") || lower.contains("blood") || lower.contains("photosynthesis") -> {
                """
                    🧬 **Biology (जीव विज्ञान) - Circulatory & Photosynthesis:**
                    
                    1. **मानव हृदय (Human Heart):**
                       • 4 कोष्ठक (Chambers) होते हैं: दायां अलिंद, दायां निलय, बायां अलिंद, बायां निलय।
                       • पल्मोनरी धमनी अशुद्ध रक्त ले जाती है, जबकि पल्मोनरी शिरा शुद्ध (ऑक्सीजन युक्त) रक्त लाती है।
                    
                    2. **प्रकाश संश्लेषण (Photosynthesis):**
                       • 6CO₂ + 6H₂O + सूर्य का प्रकाश → C₆H₁₂O₆ (ग्लूकोज) + 6O₂
                       • यह प्रक्रिया पत्तियों में मौजूद क्लोरोप्लास्ट में होती है।
                    
                    💡 कोई विशिष्ट डायग्राम या प्रश्न हल करना हो तो बताएं!
                """.trimIndent()
            }

            lower.contains("math") || lower.contains("गणित") || lower.contains("trigonometry") || lower.contains("formula") || lower.contains("calculus") -> {
                """
                    📐 **Mathematics (गणित) - Essential Formulas:**
                    
                    1. **त्रिकोणमिति सर्वसमिकाएं (Trigonometric Identities):**
                       • sin²θ + cos²θ = 1
                       • 1 + tan²θ = sec²θ
                       • 1 + cot²θ = cosec²θ
                    
                    2. **द्विघात समीकरण (Quadratic Formula):**
                       • ax² + bx + c = 0 ⇒ x = [-b ± √(b² - 4ac)] / (2a)
                    
                    3. **क्षेत्रमिति (Mensuration):**
                       • वृत्त का क्षेत्रफल = πr²
                       • बेलन (Cylinder) का आयतन = πr²h
                    
                    ✍️ अपने गणितीय प्रश्न या समीकरण को सीधे टाइप करें, मैं चरणबद्ध हल करूँगा।
                """.trimIndent()
            }

            lower.contains("bpsc") || lower.contains("bihar police") || lower.contains("bihar") || lower.contains("बिहार") -> {
                """
                    🏛️ **BPSC & Bihar Police Exam Special (बिहार विशेष):**
                    
                    1. **स्थापना एवं राजधानी:**
                       • बिहार की स्थापना 22 मार्च 1912 को हुई (बिहार दिवस: 22 मार्च)।
                       • राजधानी: पटना (प्राचीन नाम पाटलिपुत्र, संस्थापक: उदायिन)।
                    
                    2. **ऐतिहासिक तथ्य:**
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
                    🎯 **Railway & SSC Exam (General Knowledge & GS):**
                    
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
                    🎓 **SPA AI TEACHER (Mithila Academy by SP)**
                    
                    नमस्ते! मैंने आपके प्रश्न: "$prompt" का विश्लेषण किया है।
                    
                    मैं आपकी निम्नलिखित विषयों में पूर्ण सहायता कर सकता हूँ:
                    • 🔬 **Physics, Chemistry, Biology** (Concept & Numerical)
                    • 📐 **Mathematics** (Algebra, Geometry, Trigonometry, Calculus)
                    • 🏛️ **BPSC, Bihar Police & State Exams GK**
                    • 🚂 **Railway & SSC General Studies**
                    
                    कृपया अपना प्रश्न थोड़ा और विस्तार से लिखें ताकि मैं आपको सबसे सटीक और चरणबद्ध उत्तर दे सकूँ!
                """.trimIndent()
            }
        }
    }
}
