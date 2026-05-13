# Namma Santhe Ledger - ProGuard Rules
# ═══════════════════════════════════════

# ── Room Database ─────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── Hilt ──────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Kotlin Serialization ─────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.nammasantheledger.**$$serializer { *; }
-keepclassmembers class com.example.nammasantheledger.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.nammasantheledger.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Coroutines ────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ── Navigation Compose Routes ─────────
-keep class com.example.nammasantheledger.navigation.Screen { *; }
-keep class com.example.nammasantheledger.navigation.Screen$* { *; }

# ── General ───────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class * implements android.os.Parcelable { *; }