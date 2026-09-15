# SubHunt ProGuard Rules

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# RevenueCat KMP
-keep class com.revenuecat.purchases.** { *; }
-keep class com.subhunt.app.billing.** { *; }
-keepattributes *Annotation*

# DataStore
-keep class androidx.datastore.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Glance Widget
-keep class androidx.glance.** { *; }
-keep class com.subhunt.app.widget.** { *; }

# Keep domain models for Room
-keep class com.subhunt.app.domain.model.** { *; }
-keep class com.subhunt.app.data.local.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
