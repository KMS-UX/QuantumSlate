# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep data classes
-keep class com.quantumslate.dashboard.data.** { *; }
-keep class com.quantumslate.dashboard.domain.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# ---- Added during Phase G, when minification was first enabled ----

# Gson relies on reflection over field names; without this, R8 renames the fields of the
# DTOs and every API response silently parses to nulls in release builds only.
-keepclassmembers,allowobfuscation class com.quantumslate.dashboard.data.remote.** {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes *Annotation*, InnerClasses
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retrofit interfaces are implemented by reflection at runtime.
-keep,allowobfuscation interface com.quantumslate.dashboard.data.remote.**
-keepattributes AnnotationDefault

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# Compose
-dontwarn androidx.compose.**

# The AppWidgetProvider is instantiated by the system from the manifest name.
-keep class com.quantumslate.dashboard.widget.TimeWeatherWidget { *; }

# The foreground service and OAuth redirect activity are likewise resolved by name.
-keep class com.quantumslate.dashboard.work.RealtimeSyncService { *; }
-keep class com.quantumslate.dashboard.data.remote.spotify.SpotifyRedirectActivity { *; }

# Tink (pulled in by androidx.security:security-crypto for EncryptedSharedPreferences)
# references Error Prone annotations that are compile-only and absent at runtime.
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.crypto.tink.** { *; }

# Tink's optional KeysDownloader references Google HTTP client and Joda-Time. This app never
# calls it (keys are generated on-device by the Android keystore), so the references are
# unreachable — suppress rather than pulling in two libraries that would ship unused.
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**
