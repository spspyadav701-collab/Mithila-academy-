package com.example.data.model

import java.util.Locale

/**
 * AppLanguage represents all supported languages within Mithila Academy application.
 */
enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val region: String,
    val speechLocaleTag: String,
    val flagEmoji: String
) {
    HINDI(
        code = "hi",
        nativeName = "हिन्दी",
        englishName = "Hindi",
        region = "भारत (India)",
        speechLocaleTag = "hi-IN",
        flagEmoji = "🇮🇳"
    ),
    ENGLISH(
        code = "en",
        nativeName = "English",
        englishName = "English",
        region = "India & Global",
        speechLocaleTag = "en-IN",
        flagEmoji = "🌐"
    ),
    MAITHILI(
        code = "mai",
        nativeName = "मैथिली",
        englishName = "Maithili",
        region = "दरभंगा / मिथिलांचल (Bihar)",
        speechLocaleTag = "hi-IN",
        flagEmoji = "🪷"
    ),
    BHOJPURI(
        code = "bho",
        nativeName = "भोजपुरी",
        englishName = "Bhojpuri",
        region = "बिहार एवं पूर्वांचल",
        speechLocaleTag = "hi-IN",
        flagEmoji = "🌾"
    ),
    SANSKRIT(
        code = "sa",
        nativeName = "संस्कृतम्",
        englishName = "Sanskrit",
        region = "प्राचीन वैदिक ज्ञान",
        speechLocaleTag = "hi-IN",
        flagEmoji = "🕉️"
    ),
    BENGALI(
        code = "bn",
        nativeName = "বাংলা",
        englishName = "Bengali",
        region = "পশ্চিমবঙ্গ / ত্রিপুরা",
        speechLocaleTag = "bn-IN",
        flagEmoji = "🇧🇩"
    ),
    URDU(
        code = "ur",
        nativeName = "اردو",
        englishName = "Urdu",
        region = "ہندوستان",
        speechLocaleTag = "ur-IN",
        flagEmoji = "🌙"
    );

    val ttsLocale: Locale
        get() = when (this) {
            HINDI, MAITHILI, BHOJPURI, SANSKRIT -> Locale("hi", "IN")
            ENGLISH -> Locale("en", "IN")
            BENGALI -> Locale("bn", "IN")
            URDU -> Locale("ur", "IN")
        }

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: HINDI
        }
    }
}
