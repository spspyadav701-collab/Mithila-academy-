# ============================================================================
# ProGuard & R8 Obfuscation & Security Hardening Rules for Mithila Academy App
# ============================================================================

# 1. Bytecode Obfuscation & Anti-Reverse Engineering
-repackageclasses 'com.example.internal.o'
-allowaccessmodification
-overloadaggressively
-flattenpackagehierarchy 'com.example.internal'

# 2. Strip Source File and Line Number metadata to prevent decompilation mapping
-renamesourcefileattribute 'SPA_SECURE'
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# 3. Strip sensitive debugging and verbose log output from release binaries
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 4. Protect Room Database & Entities
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# 5. Protect Moshi & Serialization Adapters
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep class * extends com.squareup.moshi.JsonAdapter { *; }

# 6. Protect Jetpack Compose Runtime and Metadata
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
}

# 7. Protect OkHttp and Retrofit Network Models
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# 8. Protect BuildConfig generated secrets
-keep class com.example.BuildConfig {
    public static final java.lang.String GEMINI_API_KEY;
}

# 9. Secure Application Components
-keep public class com.example.MainActivity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

