
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Supabase & Ktor & Kotlinx Serialization
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Compose
-keep class androidx.compose.** { *; }
