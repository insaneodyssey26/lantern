# Keep Room entities and DAOs
-keepclassmembers class * extends androidx.room.RoomDatabase {
    *;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Hilt entry points and generated components
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.ComponentManager { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep Compose view models & state
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
