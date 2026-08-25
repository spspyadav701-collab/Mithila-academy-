package com.example.util

import com.example.data.model.AppLanguage

/**
 * LanguageStrings provides UI localized text across all supported application languages.
 */
object LanguageStrings {

    fun getTabTitle(tabName: String, language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> when (tabName) {
                "Home" -> "होम"
                "All Courses" -> "सभी कोर्सेस"
                "My Courses" -> "मेरे कोर्सेस"
                "Notes & PDFs" -> "नोट्स एवं PDF"
                "Test Series" -> "टेस्ट सीरीज़"
                "Free Videos" -> "फ्री वीडियो"
                "Free Notes" -> "फ्री नोट्स"
                "AI Doubts (Live Teacher)" -> "AI टीचर (डाउट्स)"
                "Live Class Room" -> "लाइव क्लास"
                "All Video Lectures" -> "वीडियो लेक्चर्स"
                "Downloads" -> "डाउनलोड्स"
                "Notice Board" -> "नोटिस बोर्ड"
                "Teacher Admin Panel" -> "टीचर एडमिन पैनल"
                "Settings & Language" -> "सेटिंग्स एवं भाषा"
                else -> tabName
            }
            AppLanguage.MAITHILI -> when (tabName) {
                "Home" -> "घर (होम)"
                "All Courses" -> "सभ कोर्सेस"
                "My Courses" -> "हमर कोर्सेस"
                "Notes & PDFs" -> "नोट्स आ PDF"
                "Test Series" -> "टेस्ट सीरीज़"
                "Free Videos" -> "मुफ्त वीडियो"
                "Free Notes" -> "मुफ्त नोट्स"
                "AI Doubts (Live Teacher)" -> "AI गुरु (शंका समाधान)"
                "Live Class Room" -> "लाइव क्लास"
                "All Video Lectures" -> "सभ वीडियो"
                "Downloads" -> "डाउनलोड्स"
                "Notice Board" -> "सूचना पट्ट"
                "Teacher Admin Panel" -> "गुरुजी एडमिन पैनल"
                "Settings & Language" -> "सेटिंग्स आ भाषा"
                else -> tabName
            }
            AppLanguage.BHOJPURI -> when (tabName) {
                "Home" -> "घर (होम)"
                "All Courses" -> "कुल कोर्स"
                "My Courses" -> "हमार कोर्स"
                "Notes & PDFs" -> "नोट्स अउर PDF"
                "Test Series" -> "टेस्ट सीरिज"
                "Free Videos" -> "फ्री वीडियो"
                "Free Notes" -> "फ्री नोट्स"
                "AI Doubts (Live Teacher)" -> "AI मास्टर साहब"
                "Live Class Room" -> "लाइव क्लास"
                "All Video Lectures" -> "कुल वीडियो क्लास"
                "Downloads" -> "डाउनलोड"
                "Notice Board" -> "सूचना पटल"
                "Teacher Admin Panel" -> "मास्टर एडमिन"
                "Settings & Language" -> "सेटिंग्स अउर भाषा"
                else -> tabName
            }
            AppLanguage.SANSKRIT -> when (tabName) {
                "Home" -> "गृहम् (मुखपृष्ठम्)"
                "All Courses" -> "सर्वे पाठाः"
                "My Courses" -> "मम पाठाः"
                "Notes & PDFs" -> "टिप्पण्यः पुस्तकानि च"
                "Test Series" -> "परीक्षा शृङ्खला"
                "Free Videos" -> "निःशुल्क दृश्यावली"
                "Free Notes" -> "निःशुल्क टिप्पण्यः"
                "AI Doubts (Live Teacher)" -> "AI आचार्यः"
                "Live Class Room" -> "प्रत्यक्ष कक्षा"
                "All Video Lectures" -> "पाठ्य दृश्यानि"
                "Downloads" -> "संगृहीतम्"
                "Notice Board" -> "सूचना फलकम्"
                "Teacher Admin Panel" -> "आचार्य फलकम्"
                "Settings & Language" -> "व्यवस्था भाषा च"
                else -> tabName
            }
            AppLanguage.BENGALI -> when (tabName) {
                "Home" -> "হোম"
                "All Courses" -> "সব কোর্স"
                "My Courses" -> "আমার কোর্স"
                "Notes & PDFs" -> "নোট এবং PDF"
                "Test Series" -> "টেস্ট সিরিজ"
                "Free Videos" -> "ফ্রি ভিডিও"
                "Free Notes" -> "ফ্রি নোট"
                "AI Doubts (Live Teacher)" -> "AI শিক্ষক (সন্দেহ)"
                "Live Class Room" -> "লাইভ ক্লাস"
                "All Video Lectures" -> "সব ভিডিও লেকচার"
                "Downloads" -> "ডাউনলোড"
                "Notice Board" -> "নোটিশ বোর্ড"
                "Teacher Admin Panel" -> "শিক্ষক অ্যাডমিন"
                "Settings & Language" -> "সেটিংস ও ভাষা"
                else -> tabName
            }
            AppLanguage.URDU -> when (tabName) {
                "Home" -> "ہوم"
                "All Courses" -> "تمام کورسز"
                "My Courses" -> "میرے کورسز"
                "Notes & PDFs" -> "نوٹس اور پی ڈی ایف"
                "Test Series" -> "ٹیسٹ سیریز"
                "Free Videos" -> "مفت ویڈیوز"
                "Free Notes" -> "مفت نوٹس"
                "AI Doubts (Live Teacher)" -> "AI استاد"
                "Live Class Room" -> "لائیو کلاس روم"
                "All Video Lectures" -> "تمام ویڈیو لیکچرز"
                "Downloads" -> "ڈاؤن لوڈز"
                "Notice Board" -> "نوٹس بورڈ"
                "Teacher Admin Panel" -> "استاد ایڈمن پینل"
                "Settings & Language" -> "سیٹنگز اور زبان"
                else -> tabName
            }
            AppLanguage.ENGLISH -> tabName
        }
    }

    fun getSettingsHeading(language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> "ऐप सेटिंग्स एवं भाषा"
            AppLanguage.MAITHILI -> "ऐप सेटिंग्स आ भाषा"
            AppLanguage.BHOJPURI -> "ऐप सेटिंग्स अउर भाषा"
            AppLanguage.SANSKRIT -> "अनुप्रयोग व्यवस्था भाषा च"
            AppLanguage.BENGALI -> "অ্যাপ সেটিংস ও ভাষা"
            AppLanguage.URDU -> "ایپ سیٹنگز اور زبان"
            AppLanguage.ENGLISH -> "Settings & Language"
        }
    }

    fun getLanguageSettingsTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> "भाषा प्राथमिकता (Language Settings)"
            AppLanguage.MAITHILI -> "भाषा चयन (Language Selection)"
            AppLanguage.BHOJPURI -> "भाषा चुने के विकल्प (Language)"
            AppLanguage.SANSKRIT -> "भाषा चयनम् (Language Settings)"
            AppLanguage.BENGALI -> "ভাষা পছন্দ (Language Preference)"
            AppLanguage.URDU -> "زبان کی ترتیبات (Language Settings)"
            AppLanguage.ENGLISH -> "Language Preference"
        }
    }

    fun getLanguageChangeSuccess(language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> "भाषा सफलतापूर्वक हिन्दी में बदली गई।"
            AppLanguage.MAITHILI -> "भाषा सफलतापूर्वक मैथिली में बदलि गेल।"
            AppLanguage.BHOJPURI -> "भाषा सफलतापूर्वक भोजपुरी में बदल दिहल गइल।"
            AppLanguage.SANSKRIT -> "भाषा संस्कृतम् रूपेण सफलीकृता।"
            AppLanguage.BENGALI -> "ভাষা সফলভাবে বাংলায় পরিবর্তন করা হয়েছে।"
            AppLanguage.URDU -> "زبان کامیابی کے ساتھ اردو میں تبدیل کر دی گئی۔"
            AppLanguage.ENGLISH -> "Language successfully switched to English."
        }
    }

    fun getVoiceGreeting(language: AppLanguage): String {
        return when (language) {
            AppLanguage.HINDI -> "नमस्ते! मैं मिथिला अकादमी का AI टीचर हूँ और मुझे SP ने बनाया है। आपका क्या सवाल है?"
            AppLanguage.MAITHILI -> "प्रणाम! हम मिथिला अकादमी के AI टीचर छी आ हमरा SP द्वारा बनाओल गेल अछि। अहाँक की प्रश्न अछि?"
            AppLanguage.BHOJPURI -> "प्रणाम! हम मिथिला एकेडमी के AI मास्टर साहब हईं आ हमरा के SP बनवले बाड़न। राउर का सवाल बा?"
            AppLanguage.SANSKRIT -> "नमो नमः! अहं मिथिला अकादमी संस्थायाः AI आचार्यः अस्मि, मां SP निर्मितवान्। भवतः कः प्रश्नः अस्ति?"
            AppLanguage.BENGALI -> "নমস্কার! আমি মিথিলা একাডেমির AI শিক্ষক, আমাকে SP তৈরি করেছেন। আপনার কি প্রশ্ন আছে?"
            AppLanguage.URDU -> "آداب! میں متھلا اکیڈمی کا AI استاد ہوں اور مجھے SP نے بنایا ہے۔ آپ کا کیا سوال ہے؟"
            AppLanguage.ENGLISH -> "Hello! I am the AI Teacher for Mithila Academy, created by SP. How can I help you today?"
        }
    }
}
