# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.example.shakeup.data.entity.** { *; }

# Hilt
-keep class * extends com.google.dagger.hilt.android.internal.managers.ViewComponentManager
-keep class dagger.hilt.android.internal.lifecycle.HiltViewModelFactory

# Kotlinx Serialization
-keepattributes *Annotation*, ElementValueAttribute
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
